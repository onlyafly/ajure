(ns ajure.module.syntax-highlighting
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.graphics Point)
           (org.eclipse.swt.custom StyledText StyleRange LineStyleListener
                                   LineStyleEvent ExtendedModifyListener
                                   VerifyKeyListener)
           (org.eclipse.swt.events SelectionAdapter KeyAdapter KeyEvent)
           (org.eclipse.swt.widgets Label Canvas Text Button)
           (org.eclipse.swt.layout GridLayout GridData))
  (:require (ajure.core [document :as document]
                        [tabs :as tabs])
            (ajure.gui [resources :as resources]
                       [status-bar :as status-bar]
                       [text-editor :as text-editor]
                       [hooks :as hooks]
                       [search-text-box :as stb])
            (ajure.util [swt :as swt]))
  (:use (ajure.gui [access :only (def-new-menu def-append-menu)])
        (ajure.util other)))

(defn- get-highlight-style-range [begin len]
  (let [range (StyleRange.)]
    (set! (. range start) begin)
    (set! (. range length) len)
    (set! (. range background) (@resources/colors :red))
    range))

(defn- get-matching-style-ranges [line-string line-offset]
  (let [line line-string
        search "("]
    (loop [offset 0
           ranges []]
      (let [result (.indexOf line search offset)]
        (if (>= result 0)
          (let [range (get-highlight-style-range (+ line-offset result) 
                                                 (.length search))]
            (recur (inc result)
                   (conj ranges range)))
          ranges)))))

(defn- document-creation-action [document-data]
  (let [style-range-function-map-ref (document-data :style-range-function-map)]
    (println "document creation action: " (document-data :docname))
    (dosync
     (commute style-range-function-map-ref
              assoc :syntax-highlighting get-matching-style-ranges))))

(defn init []
  (document/add-creation-action :syntax-highlighting document-creation-action)
  (tabs/for-each-tab document-creation-action))
