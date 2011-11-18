;; ajure.core.tabs
;;
;; Should:
;;  - Manage multiple tabs

(ns ajure.core.tabs
  (:require (ajure.core [undo :as undo]
                        [file-utils :as file]
                        [scripts :as scripts]
                        [recent :as recent])
            (ajure.gui [file-dialogs :as file-dialogs]
                       [resources :as resources]
                       [info-dialogs :as info-dialogs]
                       [status-bar :as status-bar]
                       [text-editor :as text-editor]
                       [tab :as tab]
                       [tab-folder :as tab-folder]
                       [access :as access])
            (ajure.state [document-state :as document-state]
                         [hooks :as hooks])
            (ajure.util [platform :as platform]
                        [swt :as swt]
                        [io :as io]
                        [text-format :as text-format]))
  (:use ajure.util.other))

;; Forward declarations
(declare do-open
         do-new
         do-save
         verify-tab-saved-and-close
         open-file-in-new-tab
         open-file-paths-in-tabs)

(defn update-tab-text
  ([]
     (update-tab-text (tab-folder/current-tab)))
  ([tab-item]
     (let [doc-id (.getData tab-item)
	       doc (@document-state/docs doc-id)
           doc-name (doc :docname)
           modified-flag (if (doc :modified) "* " "")]
       (.setText tab-item (str modified-flag doc-name)))))

(defn update-tab-status [tab]
  (let [doc-id (.getData tab)
        doc (@document-state/docs doc-id)
        charset-name (doc :charset)
        line-ending-name (text-format/get-line-ending-name (doc :endings))]
    (status-bar/update-current-endings (str line-ending-name
                                            " / "
                                            charset-name))))

(defn set-modified-status
  ([state]
     (set-modified-status (tab-folder/current-tab) state))
  ([tab state]
     (let [doc-id (.getData tab)]
       (dosync
         (commute document-state/docs
                  assoc-in [doc-id :modified] state))
       (update-tab-text tab))))

(defn get-all-tabs-data-values [key]
  (let [tabs (tab-folder/all-tabs)
        items (map #(key (@document-state/docs (.getData %)))
                   tabs)]
    items))

(defn for-each-textbox [action]
  (let [textboxes (get-all-tabs-data-values :textbox)]
    (doseq [textbox textboxes]
      (action textbox))))

(defn- get-all-open-doc-ids []
  (let [tabs (tab-folder/all-tabs)
        doc-ids (map #(.getData %) tabs)]
    doc-ids))

(defn for-each-tab [action]
  (let [doc-ids (get-all-open-doc-ids)]
    (dosync
     (doseq [doc-id doc-ids]
       (commute document-state/docs
                update-in [doc-id] action)))))

(defn tab-selected-action [selected-tab]
  (document-state/set-current-doc-id (.getData selected-tab))
  (.setFocus (document-state/current :textbox))
  (update-tab-status selected-tab))

(defn select-tab-with-file-path [file-path]
  (loop [tabs (tab-folder/all-tabs)]
    (let [tab (first tabs)
          more-tabs (next tabs)
		  doc-id (.getData tab)
		  doc (@document-state/docs doc-id)]
      (if (= file-path (:filepath doc))
        (do
          (.setSelection @hooks/tab-folder tab)
          (tab-selected-action tab))
        (when more-tabs
          (recur more-tabs))))))

(defn on-text-box-change [old-text start length]
  (let [new-text (if (zero? length)
                   ""
                   (.getTextRange (document-state/current :textbox) start length))]
    (undo/do-text-change old-text new-text start length)))

(defn on-text-box-verify-key [event]    
  (swt/execute-key-combo-in-mappings event
                                     @access/editor-key-combos
                                     #(do nil)))

(defn close-an-unused-tab-if-replaced []
  (let [tabs (tab-folder/all-tabs)]
    ; We only want to close an unused tab if there are two tabs:
    ; the new tab and an unused one.
    (if (= 2 (tab-folder/tab-count))
      (loop [remaining-tabs tabs]
        (if remaining-tabs
          (let [tab (first remaining-tabs)
                doc-id (.getData tab)
				doc (@document-state/docs doc-id)
                file-path (doc :filepath)
                is-modified (doc :modified)]
            (if (and (not is-modified)
                     (nil? file-path))
              (.dispose tab)
              (recur (next remaining-tabs)))))))))

(defn- get-style-range-functions []
  (let [style-range-function-map (document-state/current :style-range-function-map)]
    (if style-range-function-map
      (vals style-range-function-map)
      [])))

(defn open-blank-file-in-new-tab []
  (let [doc-name (document-state/get-unique-name)
        [tab canvas text numbering] (tab/create-tab doc-name
                                                    #(set-modified-status true)
                                                    on-text-box-verify-key
                                                    on-text-box-change
                                                    open-file-paths-in-tabs
                                                    get-style-range-functions)
        doc-id (document-state/make-blank-doc text numbering
                                              canvas doc-name)]
    (.setData tab doc-id)
    (.setFocus text)
    (document-state/set-current-doc-id doc-id)
    (update-tab-status tab)))

(defn file-already-open? [file-path]
  (let [opened-file-paths (get-all-tabs-data-values :filepath)
        matching-file-paths (map #(= file-path %) opened-file-paths)]
    (any-true? matching-file-paths)))

(defn update-recent-files-menu []
  (access/remove-menu-children "File" "Recent Files")
  (doseq [file-path (@hooks/settings :recent-files)]
    (let [file-name (io/get-file-name-only file-path)]
      (access/def-append-sub-menu "File" "Recent Files"
                                  (:item file-name
                                         (open-file-in-new-tab file-path))))))

(defn open-file-in-new-tab [file-name]
  (if (file-already-open? file-name)
    ; If the file is already open, bring its tab to the front
    (select-tab-with-file-path file-name)
    
    ; If the file is not already open, open it in a new tab
    (when file-name
      (cond
        (not (io/file-exists? file-name))
          (info-dialogs/warn-file-not-exists file-name)
        (not (io/file-readable? file-name))
          (info-dialogs/warn-file-not-readable file-name)
        :else
          (if (io/file-readable? file-name)
            (let [[content charset]
                    (io/read-content-and-charset-of-text-file file-name)
                  [tab canvas text numbering] (tab/create-tab file-name
                                                              #(set-modified-status true)
                                                              on-text-box-verify-key
                                                              on-text-box-change
                                                              open-file-paths-in-tabs
                                                              get-style-range-functions)
                  [dir name] (io/get-file-name-parts file-name)
                  content-line-endings (text-format/determine-line-endings content)
                  doc-id (document-state/make-doc text numbering
                                                  canvas name
                                                  file-name dir
                                                  content-line-endings
                                                  charset)]
              (.setData tab doc-id)
              (.setFocus text)
              (document-state/set-current-doc-id doc-id)
              (update-tab-status tab)

              (recent/add-recent-file file-name)
              (update-recent-files-menu)

              ;; 1. The extended modify listener is temporarily removed so that
              ;;    no change will be registered.
              ;; 2. The current document must be set prior to the text being set
              ;;    so that the current text-box is set before the modify event
              ;;    is fired
              (text-editor/pause-change-listening text)
              (.setText text content)
              (text-editor/redraw-line-numbering numbering)
              (text-editor/resume-change-listening text on-text-box-change)

              (set-modified-status false)

              (close-an-unused-tab-if-replaced)))))))

(defn open-file-paths-in-tabs [file-paths]
  (doseq [path file-paths]
    (when (io/file-not-directory? path)
      (open-file-in-new-tab path))))

(defn do-new []
  (open-blank-file-in-new-tab))

(defn do-open []
  (let [file-name (file-dialogs/open-dialog "Open" (document-state/current :directory))]
    (open-file-in-new-tab file-name)))

(defn do-save-as
  ([]
     (do-save-as (tab-folder/current-tab)))
  ([tab-item]
     (if tab-item
       (let [doc-id (.getData tab-item)
	     doc (@document-state/docs doc-id)
             content (.getText (doc :textbox))
             old-doc-name (doc :docname)
             old-doc-dir (doc :directory)
             file-name (file-dialogs/save-dialog
                         (str "Save <" old-doc-name "> As...")
                         old-doc-dir
                         old-doc-name)]
         (if file-name
           (do
             (let [[dir doc-name] (io/get-file-name-parts file-name)]
               (dosync
                (commute document-state/docs
                         assoc-in [doc-id]
                         :filepath file-name
                         :directory dir
                         :docname doc-name)))
             (update-tab-text tab-item)
             (let [endings (doc :endings)
                   charset (doc :charset)
                   updated-content (text-format/change-line-endings content endings)]
               (io/write-text-file file-name updated-content charset))
             (set-modified-status tab-item false)

             (recent/add-recent-file file-name)
             (update-recent-files-menu)))))))

(defn do-save
  ; Save the current tab item
  ([]
     (do-save (tab-folder/current-tab)))
  ; Save the given tab item
  ([tab-item]
     (if tab-item
       (let [doc-id (.getData tab-item)
	         doc (@document-state/docs doc-id)
             content (.getText (doc :textbox))
             file-name (doc :filepath)]
         (if file-name
           (do
             (let [endings (doc :endings)
                   charset (doc :charset)
                   updated-content (text-format/change-line-endings content endings)]
               (io/write-text-file file-name updated-content charset))
             (set-modified-status tab-item false))
           (do-save-as tab-item))))))

(defn do-save-all []
  (loop [tabs (tab-folder/all-tabs)]
    (doseq [tab tabs]
      (let [doc-id (.getData tab)
	        doc (@document-state/docs doc-id)
            is-modified (doc :modified)]
        (if is-modified
          (do-save tab))))))

(defn tab-modified?
  ([]
     (tab-modified? (tab-folder/current-tab)))
  ([tab-item]
     (let [doc-id (.getData tab-item)
	   doc (@document-state/docs doc-id)
           is-modified (doc :modified)]
       is-modified)))

(defn any-tabs-modified? []
  (let [modified-states (get-all-tabs-data-values :modified)]
    (if (any-true? modified-states)
      true
      false)))

(defn change-current-tab-line-endings [line-endings]
  (let [tab (tab-folder/current-tab)
        doc-id (.getData tab)]
    (dosync
      (commute document-state/docs
	           assoc-in [doc-id :endings] line-endings))
    (update-tab-status tab)))

(defn verify-all-tabs-saved-before-action [action]
  (if (any-tabs-modified?)
    (info-dialogs/confirm-action "Warning" "Do you want to save all open documents?"
                           do-save-all
                           action
                           #(do nil))
    (action)))

(defn verify-current-tab-saved-before-action [action]
  (if (document-state/current :modified)
    (info-dialogs/confirm-action "Warning" "Do you want to save the current document?"
                           do-save
                           action
                           #(do nil))
    (action)))

(defn verify-all-tabs-saved-then-close? []
  (if (any-tabs-modified?)
    (info-dialogs/confirm-action 
      "Warning" "Do you want to save all open documents?"
      #(do
         (do-save-all)
         false)
      (constantly true)
      (constantly false))
    true))

(defn verify-tab-saved-then-close? [tab-item]
  (if (tab-modified? tab-item)
    (info-dialogs/confirm-action
      "Warning" "Do you want to save the document before closing?"
      #(do
         (do-save tab-item)
         false)
      (constantly true)
      (constantly false))
    true))

(defn verify-tab-saved-and-close []
  (let [tab-item (tab-folder/current-tab)
        close-fn (fn []
                   (do
                     (if (= 1 (tab-folder/tab-count))
                       (open-blank-file-in-new-tab))
                     (.dispose tab-item)))]
    (if (tab-modified? tab-item)
      (info-dialogs/confirm-action
        "Warning" "Do you want to save the document before closing?"
        #(do-save tab-item)
        #(close-fn)
        #(do nil))
      (close-fn))))