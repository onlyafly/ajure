;; ajure.core.project

(ns ajure.core.project
  (:require (ajure.core [scripts :as scripts]
                        [recent :as recent])
            (ajure.gui [file-tree :as file-tree]
                       [file-dialogs :as file-dialogs]
                       [info-dialogs :as info-dialogs]
                       [status-bar :as status-bar]
                       [access :as access])
            (ajure.state [hooks :as hooks])
            (ajure.util [io :as io]
                        [info :as info]
                        [platform :as platform]
                        [swt :as swt])))

;;---------- Constants

(def dialog-filter-names ["Ajure Project Files (*.aproj)"
                          "All Files (*.*)"])
(def dialog-filter-exts ["*.aproj" "*.*"])

;;---------- Project data

(def project-file-name (ref nil))
(def project-item-paths (ref []))
(def is-project-modified (ref false))

;;---------- Project functions

;; Forward declarations
(declare do-save-project
         open-project
         update-recent-projects-menu)

(defn project-currently-open? []
  (if (zero? (count @project-item-paths))
    false
    true))

(defn verify-project-saved-then-close!? []
  (io!
   (if @is-project-modified
     (info-dialogs/confirm-action! "Warning"
                                   "Do you want to save the current project?"
                                   #(do
                                      (do-save-project)
                                      false)
                                   (constantly true)
                                   (constantly false))
     true)))

(defn verify-project-saved-before-action [action]
  (if @is-project-modified
    (info-dialogs/confirm-action!
     "Warning" "Do you want to save the current project?"
     do-save-project
     action
     #(do nil))
    (action)))

(defn add-path-to-project [path]
  (dosync
   (commute project-item-paths conj path)
   (ref-set is-project-modified true))
  (file-tree/add-file-name-to-tree! @hooks/file-tree path))

(defn add-paths-to-project [path-vector]
  (doseq [path path-vector]
    (add-path-to-project path)))

(defn show-project-pane []
  (file-tree/show-file-tree! @hooks/sash-form @hooks/tab-folder true))

(defn hide-project-pane []
  (file-tree/show-file-tree! @hooks/sash-form @hooks/tab-folder false))

(defn update-recent-projects-menu []
  (access/remove-menu-children "File" "Recent Projects")
  (doseq [file-path (@hooks/settings :recent-projects)]
    (let [file-name (io/get-file-name-only! file-path)]
      (access/def-append-sub-menu "File" "Recent Projects"
        (:item file-name
               (open-project file-path))))))

(defn close-current-project []
  (file-tree/remove-all-tree-items! @hooks/file-tree)
  (hide-project-pane)
  (dosync
   (ref-set project-file-name nil)
   (ref-set project-item-paths [])
   (ref-set is-project-modified false)))

;; Project files should only setup the data, not the UI.  The UI should
;; be handled by opening routines
(defn save-project [file-name]
  (io/write-text-file! file-name
                       (with-out-str
                         (let []
                           (println ";; Ajure project file, version:" info/version-number-string)
                           (println ";; Automatically generated file.  Modify with care.")
                           (newline)
                           (prn `(use 'ajure.core.project))
                           (newline)
                           (prn `(add-paths-to-project ~(deref project-item-paths)))
                           (prn `(dosync
                                  (ref-set project-file-name ~file-name)
                                  (ref-set is-project-modified false))))))
  (recent/add-recent-project file-name)
  (update-recent-projects-menu)
  (status-bar/set-message! (str "Saved project to "
                                "<" file-name ">")))

(defn open-project [file-path]
  (when file-path
    (close-current-project)
    (show-project-pane)
    (scripts/try-load-file file-path)
    (recent/add-recent-project file-path)
    (update-recent-projects-menu)))

(defn do-new-project []
  (verify-project-saved-before-action
   #(let [path (file-dialogs/dir-dialog! @hooks/shell "Choose a directory for your project...")]
      (when path
        (close-current-project)
        (show-project-pane)
        (add-path-to-project path)))))

(defn do-open-project []
  (verify-project-saved-before-action
   #(let [file-path (file-dialogs/open-dialog! @hooks/shell
                                               "Open Project"
                                               platform/home-dir ""
                                               dialog-filter-names
                                               dialog-filter-exts)]
      (open-project file-path))))

(defn do-save-project-as []
  (if (project-currently-open?)
                                        ; Project open
    (let [[initial-dir initial-file-name] (if @project-file-name
                                            (io/get-file-name-parts! @project-file-name)
                                            [platform/home-dir ""])
          file-name (file-dialogs/save-dialog! @hooks/shell
                                               "Save Project As..."
                                               initial-dir
                                               initial-file-name
                                               dialog-filter-names
                                               dialog-filter-exts)]
      (when file-name
        (dosync
         (ref-set is-project-modified false)
         (ref-set project-file-name file-name))
        (save-project file-name)))

                                        ; No project open
    (swt/show-info-dialog! @hooks/shell "Save Project"
                           "There is no project currently open.")))

(defn do-save-project []
  (if (project-currently-open?)
                                        ; Project open
    (if @project-file-name
                                        ; If the project already has a name, just save it
      (save-project @project-file-name)

                                        ; If not, let the user specify one
      (do-save-project-as))
                                        ; No project open
    (swt/show-info-dialog! @hooks/shell "Save Project"
                           "There is no project currently open.")))

(defn do-close-project []
  (if (project-currently-open?)
                                        ; Project open
    (verify-project-saved-before-action close-current-project)
                                        ; No project open
    (swt/show-info-dialog! @hooks/shell "Close Project"
                           "There is no project currently open.")))
