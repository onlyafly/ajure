(ns ajure.gui.tab-folder
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.custom CTabFolder CTabFolder2Adapter)
           (org.eclipse.swt.events SelectionAdapter))
  (:require (ajure.gui [resources :as resources])
			(ajure.state [hooks :as hooks])
            (ajure.util [swt :as swt])))

(defn current-tab []
  (.getSelection @hooks/tab-folder))

(defn tab-count []
  (.getItemCount @hooks/tab-folder))

(defn all-tabs []
  (seq (.getItems @hooks/tab-folder)))

(defn create-tab-folder! [parent
                          close-tab?
                          last-tab-closing-action
                          tab-selected-action]
  (io!
   (let [tab-folder (CTabFolder. parent SWT/CLOSE)]

     ;; Color of selected and non-selected tabs
     (let [colors (into-array 
                   [(@resources/colors :azure-light)
                    (@resources/colors :azure-medium)
                    (@resources/colors :azure-dark)
                    (@resources/colors :azure-dark)])
           percents (int-array [25 50 100])]
       (.setSelectionBackground tab-folder colors percents true))
     
     (.setBackground tab-folder (@resources/colors :std-gray))

     (doto tab-folder
       ;; Toggles between curvy tabs and square tabs
       (.setSimple false)

       (.setBorderVisible false)
       
       ;; Adapter to listen for close events
       (.addCTabFolder2Listener
        (proxy [CTabFolder2Adapter] []
          (close [event]
            (let [should-close (close-tab? (. event item))]
              (set! (. event doit) should-close)
              
              ;;If tab is closing and it is the last open doc
              (if (and should-close
                       (= 1 (.getItemCount tab-folder)))
                (last-tab-closing-action))))))
       
       ;; Adapter to listen for selection events, occurs when tab is selected
       (.addSelectionListener
        (proxy [SelectionAdapter] []
          (widgetSelected [event] (tab-selected-action (. event item)))
          (widgetDefaultSelected [event] (tab-selected-action (. event item))))))

     tab-folder)))

