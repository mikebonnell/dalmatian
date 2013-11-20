(ns dalmatian
  (:require [twitter.oauth :as oauth]
            [twitter.api.streaming :as streaming]
            [cheshire.core :as json]
            [clipchat.rooms :as hipchat]
            [environ.core :refer [env]])
  (:import (twitter.callbacks.protocols AsyncStreamingCallback)))

(defn die!
  ([] (die! 1))
  ([code] (System/exit code)))

(def creds (oauth/make-oauth-creds 
             (env :app-consumer-key)
             (env :app-consumer-secret)
             (env :user-access-token)
             (env :user-access-token-secret)))

(def message-is-tweet? :user)

(def hipchat-room-id (env :hipchat-room-id))
(def hipchat-auth-token (env :hipchat-auth-token))

(defn get-poster-handle-from-tweet
  [tweet]
  (get-in tweet [:user :screen_name]))

(defn build-perma-url
  [tweet]
  (when-let [handle (get-poster-handle-from-tweet tweet)]
    (let [url (str "https://twitter.com/" handle "/status/" (:id_str tweet))]
      (str "<a href=\"" url "\">" url "</a>"))))

;; TODO include tweet preview
(defn hipchat-callback
  [tweet]
  (let [permaurl (build-perma-url tweet)]
    (hipchat/message hipchat-auth-token {:room_id hipchat-room-id :message permaurl :from "dalmatian"})))

(defn on-bodypart-fn
  "Called when a new message is received from the streaming api"
  [callbacks]
  (fn on-bodypart
    [response baos]
    (let [tweet (json/parse-string (.toString baos) true)]
      (when (message-is-tweet? tweet)
        (doseq [f callbacks]
          (f tweet))))))

(defn on-failure
  "Called when the streaming api returns a 4xx response.
   Kill this process and let Heroku bounce it to reconnect."
  [response]
  (println response)
  (die!))

(defn on-exception
  "Called when an exception is thrown.
   Kill this process and let Heroku bounce it to reconnect."
  [response throwable]
  (println (.toString throwable))
  (die!))

(defn make-async-streaming-callback
  [callbacks]
  (AsyncStreamingCallback.
    (on-bodypart-fn callbacks)
    on-failure
    on-exception))

(defn run!
  [async-streaming-callback track]
  (streaming/statuses-filter :oauth-creds creds :callbacks async-streaming-callback :params {:track track}))

(defn -main
  [& _]
  (if-let [track (env :track)]
    (let [callbacks [hipchat-callback]
          async-streaming-callback (make-async-streaming-callback callbacks)]
      (run! async-streaming-callback track))
    (do
      (println "FATAL: nothing defined for env var TRACK")
      (die! 3))))
