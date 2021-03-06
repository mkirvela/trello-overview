(ns trello-overview.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [tailrecursion.cljson :refer [clj->cljson cljson->clj]]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]))

(enable-console-print!)

(defonce app-state (atom {:cards []
                          :state "ready"}))

(defn- json-parse [s]
  (js->clj (cljson->clj s) :keywordize-keys true))

(defn cards []
  [:ul {:class (:state @app-state)}
   (for [item (:cards @app-state)]
     ^{:key item} [:li item])])

(defn- get-cards [list-name]
  (go
    (swap! app-state assoc :state "loading")
    (let [body (:body (<! (http/get (str "/cards/" list-name))))]
      (swap! app-state assoc :cards (doall (:cards (json-parse body)))
                             :state "ready"))))

(def lists ["Backlog" "In Progress" "Pending Deployment"])

(defn navi []
  [:div
   (for [l lists]
     ^{:key l} [:span {:class "navi"
                       :style {:color (if (= (:list @app-state) l)
                                        "red"
                                        "black")}
                       :on-click #(do
                                    (swap! app-state assoc :list l)
                                    (get-cards l))} l])])

(defn trello-app []
  [:div
   (navi)
   (cards)])

(reagent/render-component [trello-app]
                          (.-body js/document))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
