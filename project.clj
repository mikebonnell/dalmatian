(defproject dalmatian "0.1.0-SNAPSHOT"
  :description "A Twitter Streaming API Multiplexer"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"                     
  :main dalmatian
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cheshire "5.2.0"]
                 [clipchat "1.0.0-SNAPSHOT"]
                 [environ "0.3.0"]
                 [twitter-api "0.7.4"]])
