;; text-format
;;  - Line-ending and charset support

(ns ajure.io.text-format
  (:import (java.nio.charset Charset)
           (java.nio ByteBuffer))
  (:use ajure.other.misc))

;;---------- Line ending

(def line-ending-crlf "\r\n")
(def line-ending-cr "\r")
(def line-ending-lf "\n")
(def line-ending-default (System/getProperty "line.separator"))

(defn change-line-endings [^String text
                           ^String ending]
  (let [lines (seq (.split text "\r\n|\r|\n" -1))]
    (str-join ending lines)))

(defn determine-line-endings [^String text]
  (cond
    (str-contains? text line-ending-crlf) line-ending-crlf
    (str-contains? text line-ending-cr) line-ending-cr
    (str-contains? text line-ending-lf) line-ending-lf
    :else line-ending-default))

(defn get-line-ending-name [^String ending]
  (cond
    (= ending line-ending-crlf) "CRLF (Windows)"
    (= ending line-ending-cr) "CR (Mac Classic)"
    (= ending line-ending-lf) "LF (Unix)"))

;;---------- Charset guessing

(def utf-8-charset (Charset/forName "UTF-8"))
(def utf-16-charset (Charset/forName "UTF-16"))
(def utf-16be-charset (Charset/forName "UTF-16BE"))
(def utf-16le-charset (Charset/forName "UTF-16LE"))
(def default-charset (Charset/defaultCharset))

;; Byte order markers
(def utf-8-bom [-17 -69 -65])
(def utf-16-be-bom [-2 -1])
(def utf-16-le-bom [-1 -2])

(defn has-utf-16-be-bom? [^ByteBuffer bytes]
  (if (>= (.limit bytes) 2)
    (= [(.get bytes 0) (.get bytes 1)] utf-16-be-bom)
    false))

(defn has-utf-16-le-bom? [^ByteBuffer bytes]
  (if (>= (.limit bytes) 2)
    (= [(.get bytes 0) (.get bytes 1)] utf-16-le-bom)
    false))

(defn has-utf-8-bom? [^ByteBuffer bytes]
  (if (>= (.limit bytes) 3)
    (= [(.get bytes 0) (.get bytes 1) (.get bytes 2)] utf-8-bom)
    false))

(defn guess-charset [^ByteBuffer bytes]
  (cond
    (has-utf-16-be-bom? bytes) utf-16be-charset
    (has-utf-16-le-bom? bytes) utf-16le-charset
    (has-utf-8-bom? bytes) utf-8-charset
    :else utf-8-charset))