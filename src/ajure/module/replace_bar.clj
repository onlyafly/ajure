(ns ajure.module.replace-bar
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.graphics Point)
           (org.eclipse.swt.custom StyledText StyleRange LineStyleListener
                                   LineStyleEvent ExtendedModifyListener
                                   VerifyKeyListener)
           (org.eclipse.swt.events SelectionAdapter KeyAdapter KeyEvent)
           (org.eclipse.swt.widgets Label Canvas Text Button)
           (org.eclipse.swt.layout GridLayout GridData))
  (:require (ajure.ui 
                      [status-bar :as status-bar]
                      [tabs :as tabs]
                      [text-editor :as text-editor]
                      [search-text-box :as stb])
            (ajure.state [doc-state :as doc-state]
			             [hooks :as hooks])
            (ajure.cwt [resources :as resources])
			(ajure.util [swt :as swt]))
  (:use (clojure.contrib [core :only (dissoc-in)])
        (ajure.ui [access :only (def-new-menu def-append-menu)])
        (ajure.util other)))

(def bar (ref nil))
(def find-box-ref (ref nil))
(def replace-box-ref (ref nil))
(def match-case-button-ref (ref nil))
(def find-text (ref nil))
(def replace-text (ref nil))
(def find-case-sensitive (ref nil))

(defn mark-find-succeeded [search-string]
  (status-bar/set-message! "Found \"" search-string "\""))

(defn mark-find-failed [search-string]
  (status-bar/set-message! "Unable to find \"" search-string "\""))

(defn get-highlight-style-range [begin len]
  (let [range (StyleRange.)]
    (set! (. range start) begin)
    (set! (. range length) len)
    (set! (. range background) (resources/get-named-color @hooks/bank :yellow))
    range))

(defn get-matching-style-ranges [line-string line-offset]
  (let [search-string @find-text]
    (if (and search-string
             (not (zero? (.length search-string))))
      
      ; If there is a search, construct the style-ranges
      (let [is-case-sensitive @find-case-sensitive
            [line search] (if is-case-sensitive
                            [line-string search-string]
                            [(.toLowerCase line-string)
                             (.toLowerCase search-string)])]
        (loop [offset 0 ranges []]
          (let [result (.indexOf line search offset)]
            (if (>= result 0)
              (let [range (get-highlight-style-range (+ line-offset result) 
                                                     (.length search))]
                (recur (inc result) (conj ranges range)))
              ranges))))
      
      ; If there is no search, return and empty vector
      [])))

(defn get-index-of-next-with-wrapping [content-string search-string
                                       is-case-sensitive after-position]
  (let [[content search] (if is-case-sensitive
                           [content-string search-string]
                           [(.toLowerCase content-string) (.toLowerCase search-string)])
        pos (.indexOf content search after-position)]
    (if (>= pos 0)
      pos
      (.indexOf content search 0))))

(defn select-next-match [is-case-sensitive]
  (when (str-not-empty? @find-text)
    (let [textbox (doc-state/current :text-box)
          numbering (doc-state/current :numbering)
          content (.getText textbox)
          [current-start current-end] (swt/point->vector (.getSelection textbox))
          next-start (get-index-of-next-with-wrapping content
                                                      @find-text
                                                      is-case-sensitive
                                                      current-end)
          find-length (.length @find-text)]
      (if (>= next-start 0)
        (do
          (.setSelection textbox next-start (+ next-start find-length))
          (text-editor/redraw-line-numbering! numbering)
          (mark-find-succeeded @find-text))
        (do
          (mark-find-failed @find-text))))))

(defn replace-current-match [is-case-sensitive]
  (when (str-not-empty? @find-text)
    (let [textbox (doc-state/current :text-box)
          content (.getText textbox)
          [current-start current-end] (swt/point->vector (.getSelection textbox))
          current-selection-length (- current-end current-start)
          find-length (.length @find-text)]
      (when (not (zero? current-selection-length))
        (let [current-selected-text (.getText textbox current-start (dec current-end))]
          (when (and (str-not-empty? current-selected-text)
                     (= current-selected-text @find-text))
            (.replaceTextRange textbox current-start find-length @replace-text)))))))

(defn- init-highlighting []
  (dosync
   (commute doc-state/docs
            assoc-in [@doc-state/current-doc-id :style-range-function-map :replace-bar] get-matching-style-ranges)))

(defn- deinit-highlighting []
(dosync
   (commute doc-state/docs
            dissoc-in [@doc-state/current-doc-id :style-range-function-map :replace-bar])))

(defn on-find-attempt []
  (let [search-text (.getText @find-box-ref)
        is-case-sensitive (.getSelection @match-case-button-ref)]
    (dosync
     (ref-set find-text search-text)
     (ref-set find-case-sensitive is-case-sensitive)
     (init-highlighting))
    (.redraw (doc-state/current :text-box))
    (select-next-match is-case-sensitive)))

(defn on-replace-attempt []
  (let [local-find-text (.getText @find-box-ref)
        local-replace-text (.getText @replace-box-ref)
        is-case-sensitive (.getSelection @match-case-button-ref)]
    (dosync
     (ref-set find-text local-find-text)
     (ref-set replace-text local-replace-text)
     (ref-set find-case-sensitive is-case-sensitive)
     (init-highlighting))
    (.redraw (doc-state/current :text-box))
    (replace-current-match is-case-sensitive)
    (select-next-match is-case-sensitive)))

(defn show []
  (swt/dynamically-show-control! @bar)
  (.setFocus @find-box-ref))

(defn hide []
  (swt/dynamically-hide-control! @bar))

(defn clear []
  (.setText @find-box-ref "")
  (.setText @replace-box-ref ""))

(defn on-find-cancelled []
  (dosync
    (ref-set find-text ""))
  (.redraw (doc-state/current :text-box))
  (.setFocus (doc-state/current :text-box)))

(defn on-escape-pressed []
  (swt/dynamically-hide-control! @bar)
  (.setText @find-box-ref "")
  (on-find-cancelled))

(defn on-cancel-clicked []
  (.setText @find-box-ref "")
  (on-find-cancelled))

(defn on-enter-pressed []
  (on-find-attempt))

(defn create-find-bar [parent hint-text replace-hint-text]
  (let [canvas (Canvas. parent SWT/NONE)
        find-box (stb/create-search-text-box! canvas
                                             hint-text
                                             on-escape-pressed
                                             on-cancel-clicked
                                             on-enter-pressed)
        replace-box (stb/create-search-text-box! canvas
                                                replace-hint-text
                                                on-escape-pressed
                                                on-cancel-clicked
                                                on-enter-pressed)
        search-button (Button. canvas (swt/options PUSH))
        replace-button (Button. canvas (swt/options PUSH))
        done-button (Button. canvas (swt/options PUSH))
        match-case-button (Button. canvas (swt/options CHECK))]

    (dosync
     (ref-set bar canvas)
     (ref-set find-box-ref find-box)
     (ref-set replace-box-ref replace-box)
     (ref-set match-case-button-ref match-case-button))

    (doto search-button
      (.setText "Find")
      (.addSelectionListener
       (proxy [SelectionAdapter] []
         (widgetSelected [event]
           (on-find-attempt)))))

    (doto replace-button
      (.setText "Replace")
      (.addSelectionListener
       (proxy [SelectionAdapter] []
         (widgetSelected [event]
           (on-replace-attempt)))))

    (doto done-button
      (.setText "Close")
      (.addSelectionListener
       (proxy [SelectionAdapter] []
         (widgetSelected [event]
           (swt/dynamically-hide-control! canvas)
           (clear)
           (on-find-cancelled)))))

    (doto match-case-button
      (.setText "Match Case"))

    ;; Setup layout
    (doto canvas
      (.setLayout (let [layout (GridLayout.)]
                    (set! (. layout numColumns) 6)
                    (set! (. layout marginHeight) 5)
                    (set! (. layout marginWidth) 5)
                    (set! (. layout verticalSpacing) 5)
                    (set! (. layout horizontalSpacing) 5)
                    layout)))
    (.setLayoutData find-box (let [data (GridData. SWT/FILL SWT/FILL true true)]
                               ;;(set! (. data heightHint) 30)
                               data))
    (.setLayoutData replace-box (let [data (GridData. SWT/FILL SWT/FILL true true)]
                                  ;;(set! (. data heightHint) 30)
                                  data))
    (.setLayoutData search-button (let [data (GridData. SWT/END SWT/FILL false true)]
                                    (set! (. data widthHint) 80)
                                    data))
    (.setLayoutData replace-button (let [data (GridData. SWT/END SWT/FILL false true)]
                                     (set! (. data widthHint) 80)
                                     data))
    (.setLayoutData done-button (let [data (GridData. SWT/END SWT/FILL false true)]
                                  (set! (. data widthHint) 80)
                                  data))
    (.setLayoutData match-case-button
                    (let [data (GridData. SWT/END SWT/FILL false true)]
                      ;; (set! (. data widthHint) 90)
                      data))

    canvas))

(defn init []
  (let [find-bar (create-find-bar @hooks/shell
                                  "Enter text to find..."
                                  "Enter replacement text...")]
    (.moveAbove find-bar @hooks/status-bar-widget)
    (.setLayoutData find-bar (let [data (GridData. SWT/FILL SWT/END true false)]
                               ;; This would allow the item to span 2 columns
                               ;; (set! (. data horizontalSpan) 2)
                               data)))
  (hide)
  (def-append-menu "Edit"
    (:cascade "Find/Replace"
              (:app-combo "Find/Replace Bar"
                          [MOD1] \f
                          (show))
              (:app-combo "Find Next"
                          [MOD1] \g
                          (on-find-attempt))
              (:app-combo "Replace Next"
                          [MOD1 SHIFT] \g
                          (on-replace-attempt)))))
