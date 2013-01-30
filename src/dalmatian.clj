(ns dalmatian
  (:require [twitter.oauth :as oauth]
            [twitter.api.streaming :as streaming]
            [cheshire.core :as json]
            [clipchat.rooms :as hipchat])
  (:import (twitter.callbacks.protocols AsyncStreamingCallback)))

(defn die!
  ([] (die! 1))
  ([code] (System/exit code)))

(defn env
  "Retrieve the value of var-name from the system environment, or nil"
  ([var-name] (env var-name nil))
  ([var-name default] (or (System/getenv var-name) default)))

(def creds (oauth/make-oauth-creds 
             (env "APP_CONSUMER_KEY")
             (env "APP_CONSUMER_SECRET")
             (env "USER_ACCESS_TOKEN")
             (env "USER_ACCESS_TOKEN_SECRET")))

(def message-is-tweet? :user)

(def ^:dynamic *hipchat-room-id* (env "HIPCHAT_ROOM_ID"))
(def ^:dynamic *hipchat-auth-token* (env "HIPCHAT_AUTH_TOKEN"))

(defn get-poster-handle-from-tweet
  [tweet]
  (get-in tweet [:user :screen_name]))

(defn build-perma-url
  [tweet]
  (when-let [handle (get-poster-handle-from-tweet tweet)]
    (let [url (str "https://twitter.com/" handle "/status/" (:id_str tweet))]
      (str "<a href=\"" url "\">" url "</a>"))))

(defn hipchat-callback
  [tweet]
  (let [permaurl (build-perma-url tweet)]
    (println "Hipchat: sending link " permaurl)
    (hipchat/message *hipchat-auth-token* {:room_id *hipchat-room-id* :message permaurl :from "dalmatian"})))

(def ^:dynamic *callbacks* [hipchat-callback])

(defn on-bodypart
  "Called when a new message is received from the streaming api"
  [response baos]
  (let [tweet (json/parse-string (.toString baos) true)]
    (when (message-is-tweet? tweet)
      (hipchat-callback tweet))))

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

(def async-streaming-callback
  (AsyncStreamingCallback.
    on-bodypart
    on-failure
    on-exception))

(defn run!
  [track]
  (streaming/statuses-filter :oauth-creds creds :callbacks async-streaming-callback :params {:track track}))

(defn -main
  [& _]
  (if-let [track (env "TRACK")]
    (run! track)
    (do
      (println "FATAL: nothing defined for env var TRACK")
      (die! 3))))
