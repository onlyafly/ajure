(ns ajure.module.syntax-highlighting
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.graphics Point)
           (org.eclipse.swt.custom StyledText StyleRange LineStyleListener
                                   LineStyleEvent ExtendedModifyListener
                                   VerifyKeyListener)
           (org.eclipse.swt.events SelectionAdapter KeyAdapter KeyEvent)
           (org.eclipse.swt.widgets Label Canvas Text Button)
           (org.eclipse.swt.layout GridLayout GridData))
  (:require (ajure.core [tabs :as tabs])
            (ajure.gui [resources :as resources]
                       [status-bar :as status-bar]
                       [text-editor :as text-editor]
                       [search-text-box :as stb])
            (ajure.state [document-state :as document-state]
			             [hooks :as hooks])
			(ajure.util [swt :as swt]))
  (:use (ajure.gui [access :only (def-new-menu def-append-menu)])
        (ajure.util other)))

(defn- get-keyword-style-range [begin len]
  (let [range (StyleRange.)]
    (set! (. range start) begin)
    (set! (. range length) len)
    (set! (. range foreground) (@resources/colors :red))
    range))

(defn- get-matching-style-ranges [line-string line-offset]
  (let [line line-string
        search "keyword"]
    (loop [offset 0
           ranges []]
      (let [result (.indexOf line search offset)]
        (if (>= result 0)
          (let [range (get-keyword-style-range (+ line-offset result) 
                                                 (.length search))]
            (recur (inc result)
                   (conj ranges range)))
          ranges)))))

(defn- document-creation-action [doc]
  (assoc-in doc
            [:style-range-function-map :syntax-highlighting]
            get-matching-style-ranges))

(defn init []
  (document-state/add-creation-action :syntax-highlighting document-creation-action)
  (tabs/for-each-tab document-creation-action))
