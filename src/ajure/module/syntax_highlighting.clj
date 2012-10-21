(ns ajure.module.syntax-highlighting
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
			(ajure.util 
                        [swt :as swt]))
  (:use (ajure.ui [access :only (def-new-menu def-append-menu)])
        (ajure.util other)))

(defn- get-keyword-style-range [begin len]
  (let [range (StyleRange.)]
    (set! (. range start) begin)
    (set! (. range length) len)
    (set! (. range foreground) (resources/get-named-color @hooks/bank :red))
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

(defn- doc-initialization [doc]
  (assoc-in doc [:style-range-function-map :syntax-highlighting]
            get-matching-style-ranges))

(defn init []
  (doc-state/do-add-doc-initialization :syntax-highlighting doc-initialization)
  (tabs/for-each-tab! doc-initialization))
