(ns reagent-tutorial.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [goog.string :as gstring]
              [reagent-tutorial.tiles :as tiles]))

;; -------------------------
;; Views

(def layout-data [
                  [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 ]
                  [1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 ]
                  [0 0 0 0 0 0 1 1 1 1 1 1 1 1 1 1 1 ]
                  [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 ]
                  [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 ]])

(def position (reagent/atom {:x 0 :y 2}))

(defn row [rownum r]
  (def colnum (atom -1))
  [:div (str "position" @position)]
  [:div.row
    (for [cell r]
      (do
        (swap! colnum inc)
        (cond 
          (and (= rownum (:y @position)) (= @colnum (:x @position))) [tiles/hero]
          (= 1 cell) [tiles/tree]
          :else [tiles/empty-ground])
    ))])

(defn can-move?
  [new-x new-y]
  (and (>= new-x 0)
               (< new-x (count (first layout-data)))
               (>= new-y 0) 
               (< new-y (count layout-data))
               (= (nth (nth layout-data new-y) new-x) 0
                  )))

(def codename
  {37 "LEFT"
   38 "UP"
   39 "RIGHT"
   40 "DOWN"
   32 "SPACE"})

(defn move-hero
  [keycode]
  (let [x (:x @position)
        y (:y @position)
        x-offset (cond (= keycode "LEFT") -1 (= keycode "RIGHT") 1 :else 0)
        y-offset (cond (= keycode "UP") -1 (= keycode "DOWN") 1 :else 0)
        new-x (+ x x-offset)
        new-y (+ y y-offset)] 
    (.log js/console x y new-x new-y (can-move? new-x new-y))
    (when (can-move? new-x new-y)
      (swap! position assoc :x new-x)
      (swap! position assoc :y new-y))
    (.log js/console (str "position: " @position))))    

(defn home-page []
  [:div [:h2 "Welcome to reagent-tutorial"]
    (map-indexed (fn [idx r] [row idx r]) layout-data)
  ])
; (defn home-page []
;   [:div [:h2 "Welcome to reagent-tutorial"]
;     (for [r layout-data]
;       [row r])
;   ])

;; -------------------------
;; Routes



(.addEventListener js/document "keydown"
  (fn [e] 
    (let 
      [keypress (codename(.-keyCode e))] 
      (.log js/console (str "keypress: " keypress))
      (move-hero keypress)
    )))


(defonce page (atom #'home-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'home-page))

;; -------------------------
;; Initialize app


(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
