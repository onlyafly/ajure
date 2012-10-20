;; text-editor
;;
;; Text editor wrapper.

(ns ajure.ui.text-editor
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.widgets Display Canvas Listener)
           (org.eclipse.swt.custom StyledText StyleRange LineStyleListener
                                   LineStyleEvent ExtendedModifyListener
                                   VerifyKeyListener)
           (org.eclipse.swt.events ModifyListener PaintListener PaintEvent
                                   MenuDetectListener SelectionListener)
           (org.eclipse.swt.layout FormLayout FormData FormAttachment)
           (org.eclipse.swt.graphics GC Font Point Image))
  (:require (ajure.state [hooks :as hooks])
            (ajure.cwt [resources :as resources])
            (ajure.util [swt :as swt]))
  (:use ajure.util.other))

;;---------- Line Numbering

(defn redraw-line-numbering! [line-numbering]
  (io!
   (.redraw line-numbering)))

;; Optimized because it is called frequently
(defn- create-numbering-paint-listener! [^Display display
                                         ^StyledText textbox
                                         ^Canvas numbering]
  (reify PaintListener
    (paintControl [this event]
      (io!
       (let [^Point textbox-size (.getSize textbox)
             textbox-height (int (. textbox-size y))
             line-count (int (.getLineCount textbox))
             top-line-num (int (.getLineIndex textbox 0))
             event-x (int (. event x))
             event-y (int (. event y))
             event-width (int (. event width))
             event-height (int (. event height))
             ^Image buffer (Image. display event-width event-height)
             ^GC gc (GC. buffer)]

         (doto gc
           (.setFont (Font. display @hooks/editor-font-data))
           (.setBackground (resources/get-named-color @hooks/bank :light-gray))
           (.fillRectangle event-x event-y event-width event-height)
           (.setForeground (resources/get-named-color @hooks/bank :gray))
           (.drawLine (+ event-x event-width -1) 0
                      (+ event-x event-width -1) event-height)
           (.setForeground (resources/get-named-color @hooks/bank :black)))

         (loop [line-num top-line-num]
           (when (< line-num line-count)
             (let [offset (int (.getLinePixel textbox line-num))]
               (when (< offset textbox-height)
                 (let [incremented-line-num (int (unchecked-inc line-num))]
                   (.drawText gc (str incremented-line-num) 2 offset)
                   (recur incremented-line-num))))))

         (.drawImage (. event gc) buffer 0 0)
         (.dispose gc)
         (.dispose buffer))))))

(defn- create-line-numbering! [display parent text-box]
  ;; Specifying no background allows double buffering to work
  (let [numbering-canvas (Canvas. parent SWT/NO_BACKGROUND)]
    (.addPaintListener numbering-canvas
                       (create-numbering-paint-listener! display
                                                         text-box
                                                         numbering-canvas))
    numbering-canvas))

(defn- create-scroll-selection-listener! [line-numbering]
  (reify SelectionListener
    (widgetDefaultSelected [this event]
      (io!
       (redraw-line-numbering! line-numbering)))
    (widgetSelected [this event]
      (io!
       (redraw-line-numbering! line-numbering)))))

(defn- create-scroll-listener! [line-numbering]
  (reify Listener
    (handleEvent [this event]
      (io!
       (redraw-line-numbering! line-numbering)))))

;;---------- Line styles

(defn- create-line-style-listener! [get-style-range-functions]
  (reify LineStyleListener
    (lineGetStyle [this event]
      (io!
       (let [style-range-funcs (get-style-range-functions)
             line (. event lineText)
             line-offset (. event lineOffset)
             style-range-lists (map #(% line line-offset) style-range-funcs)
             style-ranges (apply concat style-range-lists)
             style-range-array (into-array StyleRange style-ranges)]
         (set! (. event styles) style-range-array))))))

;;---------- Text Editor

(defn- attach-popup-menu! [text-box]
  (io!
   (.addMenuDetectListener text-box
                           (reify MenuDetectListener
                             (menuDetected [this event]
                               (io!
                                (doto @hooks/popup-menu
                                  (.setLocation (. event x) (. event y))
                                  (.setVisible true))))))))

(defn- create-extended-modify-listener! [text-change-action]
  (reify ExtendedModifyListener
    (modifyText [this event]
      (io!
       (text-change-action (. event replacedText)
                           (. event start)
                           (. event length))))))

(defn pause-change-listening! [text-box]
  (io!
   ;;TODO 3000 is the listener code for ExtendedModify.  Try to find
   ;;     and named value for it.
   (let [listeners (seq (.getListeners text-box 3000))]
     (doseq [listener listeners]
       (.removeListener text-box 3000 listener)))))

(defn resume-change-listening! [text-box text-change-action]
  (io!
   (.addExtendedModifyListener text-box
                               (create-extended-modify-listener! text-change-action))))

(defn make! [display
             shell
             parent
             text-modified-action
             verify-key-action
             text-change-action
             dropped-file-paths-action
             get-style-range-functions]
  (io!
   (let [margin-canvas (Canvas. parent SWT/NONE)
         text-box (StyledText. margin-canvas (swt/options V_SCROLL H_SCROLL))
         numbering (create-line-numbering! display margin-canvas text-box)
         text-form-data (FormData.)]

     ;; Setup layout
     (doto margin-canvas
       (.setLayout (FormLayout.)))
     (doto numbering
       (.setLayoutData 
        (let [form-data (FormData.)]
          (set! (. form-data width) 40) ;width of numbering canvas
          (set! (. form-data top) (FormAttachment. 0 0))
          (set! (. form-data left) (FormAttachment. 0 0))
          (set! (. form-data bottom) (FormAttachment. 100 0))
          form-data)))
     (doto text-box
       (.setLayoutData 
        (let [form-data (FormData.)]
          (set! (. form-data top) (FormAttachment. 0 0))
          (set! (. form-data left) (FormAttachment. numbering 0))
          (set! (. form-data right) (FormAttachment. 100 0))
          (set! (. form-data bottom) (FormAttachment. 100 0))
          form-data)))

     (doto margin-canvas
       (.setBackground (.getSystemColor display SWT/COLOR_GRAY)))

     (doto text-box
       ;; TODO If a text box at any time has had word wrapping set to true,
       ;; resizing the text box is incredibly slow when there is large
       ;; amounts of text in it.  Any way around this?
       (.setWordWrap (boolean (@hooks/settings :word-wrap-enabled)))

       ;; Remove the default key binding for pasting
       (.setKeyBinding (bit-or-many SWT/MOD1 (int \v)) SWT/NULL)

       (.addExtendedModifyListener
        (create-extended-modify-listener! text-change-action))
       (.addLineStyleListener
        (create-line-style-listener! get-style-range-functions))
       (.addVerifyKeyListener
        (reify VerifyKeyListener
          (verifyKey [this event]
            (verify-key-action event))))
       (.addModifyListener
        (reify ModifyListener
          (modifyText [this modify-event]
            (text-modified-action)))))

     (let [scroll-listener (create-scroll-listener! numbering)
           scroll-selection-listener (create-scroll-selection-listener! numbering)]
       (doto text-box
         (.addListener SWT/MouseDown scroll-listener)
         (.addListener SWT/MouseUp scroll-listener)
         (.addListener SWT/MouseMove scroll-listener)
         (.addListener SWT/KeyDown scroll-listener)
         (.addListener SWT/KeyUp scroll-listener)
         (.addListener SWT/Resize scroll-listener))
       (.addSelectionListener (.getVerticalBar text-box)
                              scroll-selection-listener))
     
     (if @hooks/editor-font-data
       (.setFont text-box (Font. display @hooks/editor-font-data)))

     (attach-popup-menu! text-box)

     (swt/add-file-dropping-to-control! text-box dropped-file-paths-action)

     [margin-canvas text-box numbering])))