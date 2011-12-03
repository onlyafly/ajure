;; ajure.util.io
;;
;; Should
;;  - Provide functions to operate on files

(ns ajure.util.io
  (:import (java.io File FileReader BufferedReader FileWriter FileInputStream
                    StringWriter FileOutputStream BufferedWriter
                    OutputStreamWriter)
           (java.nio ByteBuffer CharBuffer)
           (java.nio.charset Charset)
           (java.nio.channels FileChannel FileChannel$MapMode))
  (:require (ajure.util [text-format :as text-format]))
  (:use ajure.util.other))

;; Issues:
;;  - Note that the .map method of channel remains mapped to memory
;;    even after the channel is closed.
;;    http://bugs.sun.com/view_bug.do?bug_id=4724038
(defn create-byte-buffer-from-channel-broken! [^FileChannel channel]
  (io!
   (.map channel (. FileChannel$MapMode READ_ONLY)
         0 (.size channel))))

(defn create-byte-buffer-from-channel! [^FileChannel channel]
  (io!
   (let [size (int (.size channel))
         buffer (. ByteBuffer allocate size)]
     (.read channel buffer)
     (.rewind buffer)
     buffer)))

;; Returns contents and charset name of text file.
(defn read-content-and-charset-of-text-file! [file]
  (io!
   (let [stream (FileInputStream. file)
         builder (StringBuilder.)]
     (try
       (let [^FileChannel channel (.getChannel stream)
             ^ByteBuffer buffer (create-byte-buffer-from-channel! channel)
             ^Charset charset (text-format/guess-charset buffer)
             ^CharBuffer decoded-buffer (.decode charset buffer)
             result (str decoded-buffer)]
         (.append builder result)
         [(str builder) (.name charset)])
       (finally
        (.close stream))))))

;; Returns contents of text file only
(defn read-text-file! [file]
  (io!
   (first (read-content-and-charset-of-text-file! file))))

(defn write-text-file!
  
  ;; charset-name should be one of UTF-8, UTF-16BE, or UTF-16LE
  ([file text charset-name]
     (io!
      (let [fos (FileOutputStream. file)
            osw (OutputStreamWriter. fos charset-name)
            bw (BufferedWriter. osw)]
        (.write bw text)
        (.close bw))))
  
  ;; for simple use only
  ([file text]
     (io!
      (let [fw (FileWriter. file)]
        (.write fw text)
        (.close fw)))))

; Used by error logging
(defn append-text-file! [file text]
  (io!
   (let [fw (FileWriter. file true)]
     (.write fw text)
     (.close fw))))

(defn create-empty-file-unless-exists! [file-path]
  (io!
   (let [file (File. file-path)]
     ;; Create an empty config file if not found
     (if (not (.exists file))
       (write-text-file! file "")))))

(defn get-file-name-only! [file]
  (io!
   (if (instance? File file)
     (.getName file)
     (.getName (File. file)))))

(defn get-path! [file-name]
  (io!
   (.getParent (File. file-name))))

(defn- file-path->file-object [file-object-or-path]
  (if (instance? File file-object-or-path)
    file-object-or-path
    (File. file-object-or-path)))

(defn get-file-name-parts! [file-object-or-path]
  (io!
   (let [^File file (file-path->file-object file-object-or-path)]
     [(.getParent file)
      (.getName file)])))

(defn file-not-directory!? [file-object-or-path]
  (io!
   (let [^File file (file-path->file-object file-object-or-path)]
     (and (.isFile file)
          (not (.isDirectory file))))))

(defn file-exists!? [file-object-or-path]
  (io!
   (let [^File file (file-path->file-object file-object-or-path)]
     (.exists file))))

(defn file-readable!? [file]
  (io!
   (let [file-obj (file-path->file-object file)]
     (.canRead file-obj))))

(defn file-visible!? [file]
  (io!
   (let [file-obj (file-path->file-object file)]
     (and (not (.isHidden file-obj))
          (not (.endsWith (.getName file-obj) "~"))))))
