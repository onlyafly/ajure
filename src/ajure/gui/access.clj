;; access
;;
;; Should
;;  - Store key-combo mappings, menu-items, and menu-item accelerators

(ns ajure.gui.access
  (:require (ajure.state [hooks :as hooks])
            (ajure.util [swt :as swt])))

;;---------- Internal use

;; Must be public to work in macro
(defn do-add-combo-to-map [combo-map-ref modifiers key action]
  (dosync
   (commute combo-map-ref assoc
            [modifiers key] action)))

;;---------- Key combos

;; Example:
;; (def-editor-combo [SHIFT CTRL] \w
;;   (set-word-wrap false)
;;   (show-msg-box "Notice" "You just turned off word wrap"))
(defmacro def-editor-combo [modifiers key & body]
  `(do-add-combo-to-map hooks/editor-key-combos
                        (vector ~@(map (fn [x] (symbol "org.eclipse.swt.SWT"
                                                      (str x)))
                                       modifiers))
                        ~key
                        (fn []
                          ~@body)))

(defmacro def-app-combo [modifiers key & body]
  `(do-add-combo-to-map hooks/application-key-combos
                        (vector ~@(map (fn [x] (symbol "org.eclipse.swt.SWT"
                                                      (str x)))
                                       modifiers))
                        ~key
                        (fn []
                          ~@body)))

;;---------- Menus

(defn- generate-menu-children [parent-menu-symbol items]
  (map (fn [item]
         (cond
          ;; Example
          ;; (:item "Help"
          ;;        (show-help))  
          (= (first item) :item)
          (let [item-title# (first (next item))
                item-body# (next (next item))]
            `(swt/create-menu-item! ~parent-menu-symbol
                                    ~item-title#
                                    (fn [] 
                                      ~@item-body#)))
          ;; Example
          ;; (:sep)
          (= (first item) :sep)
          `(swt/create-menu-separator! ~parent-menu-symbol)

          ;; Example
          ;; (:cond is-showing-colors
          ;;   (:item "Red" (println "r"))
          ;;   (:item "Blue" (println "b")))
          (= (first item) :cond)
          (let [cond-predicate# (first (next item))
                cond-items# (next (next item))]
            `(when ~cond-predicate#
               ~@(generate-menu-children parent-menu-symbol
                                         cond-items#)))

          ;; Example
          ;; (:cascade "Colors"
          ;;   (:item "Red" (println "r"))
          ;;   (:item "Blue" (println "b")))
          (= (first item) :cascade)
          (let [cascade-menu-title# (first (next item))
                cascade-items# (next (next item))
                cascade-menu-symbol# `cm#]
            `(let [~cascade-menu-symbol#
                   (swt/create-cascading-sub-menu! ~parent-menu-symbol
                                                   ~cascade-menu-title#)]
               ~@(generate-menu-children cascade-menu-symbol#
                                         cascade-items#)))
          
          ;; Example
          ;; (:app-combo "Help"
          ;;             [CTRL] \h
          ;;             (show-help))
          (= (first item) :app-combo)
          (let [item-title# (first (next item))
                modifiers# (first (next (next item)))
                key# (first (next (next (next item))))
                combo-string# (swt/get-combo-string modifiers# key#)
                item-body# (next (next (next (next item))))]
            `(do
               (def-app-combo ~modifiers# ~key# ~@item-body#)
               (swt/create-menu-item! ~parent-menu-symbol 
                                      (str ~item-title#
                                           "\t"
                                           ~combo-string#)
                                      (fn [] 
                                        ~@item-body#))))
          ;; Example
          ;; (:editor-combo "Help"
          ;;                [CTRL] \h
          ;;                (show-help))     
          (= (first item) :editor-combo)
          (let [item-title# (first (next item))
                modifiers# (first (next (next item)))
                key# (first (next (next (next item))))
                combo-string# (swt/get-combo-string modifiers# key#)
                item-body# (next (next (next (next item))))]
            `(do
               (def-editor-combo ~modifiers# ~key# ~@item-body#)
               (swt/create-menu-item! ~parent-menu-symbol 
                                      (str ~item-title#
                                           "\t"
                                           ~combo-string#)
                                      (fn [] 
                                        ~@item-body#))))))
       items))

;; Example:
;; (access/def-menu "Help"
;;   (:item "About Ajure"
;;     (info-dialogs/show-about-box!))
;;   (:item "Open Error Log"
;;     (tabs/open-file-in-new-tab file/error-log-file-path)))
(defmacro def-menu [title & items]
  (let [parent-menu `m#]
    `(let [~parent-menu (swt/create-sub-menu! (deref hooks/shell) 
                                              (deref hooks/menu-bar)
                                              ~title)]
       ~@(generate-menu-children parent-menu items))))

;; This sub menu will be placed before the last item in the menu
;; This allows the new menu to be placed before the Help menu
(defmacro def-new-menu [title & items]
  (let [parent-menu-symbol `m#
        sub-menu-count (.getItemCount @hooks/menu-bar)
        index (if (> sub-menu-count 0)
                (- sub-menu-count 1)
                0)]
    `(let [~parent-menu-symbol (swt/create-sub-menu! (deref hooks/shell) 
                                                     (deref hooks/menu-bar)
                                                     ~title
                                                     ~index)]
       ~@(generate-menu-children parent-menu-symbol items))))

(defmacro def-append-menu [title & items]
  (let [parent-menu-symbol `m#]
    `(let [~parent-menu-symbol (swt/get-child-sub-menu (deref hooks/menu-bar)
                                                       ~title)]
       (when ~parent-menu-symbol
         ~@(generate-menu-children parent-menu-symbol items)))))

(defmacro def-append-sub-menu [title sub-menu-title & items]
  (let [parent-menu-symbol `pm#
        sub-menu-symbol `sm#]
    `(let [~parent-menu-symbol (swt/get-child-sub-menu (deref hooks/menu-bar)
                                                       ~title)
           ~sub-menu-symbol (swt/get-child-sub-menu ~parent-menu-symbol
                                                    ~sub-menu-title)]
       (when ~sub-menu-symbol
         ~@(generate-menu-children sub-menu-symbol items)))))

(defn remove-menu-children
  ([parent-menu-title]
     (let [parent-menu (swt/get-child-sub-menu @hooks/menu-bar
                                               parent-menu-title)]
       (when parent-menu
         (doseq [menu-item (seq (.getItems parent-menu))]
           (.dispose menu-item)))))
  ([parent-menu-title sub-menu-title]
     (let [parent-menu (swt/get-child-sub-menu @hooks/menu-bar
                                               parent-menu-title)
           sub-menu (swt/get-child-sub-menu parent-menu
                                            sub-menu-title)]
       (when sub-menu
         (doseq [menu-item (seq (.getItems sub-menu))]
           (.dispose menu-item))))))