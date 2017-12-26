(ns plawww.puzzle
  (:require
   [reagent.core :as r]))

(def questions
  [{:text "Care este diferenta dintre 17 si 4 ? "
    :ans 13
    :time 30}
   {:text "185 + 120 = ?"
    :ans 305}
   {:text "1002 + 1000 = ?"
    :ans 2002}
   {:text "56 + 43 = ?"
    :ans 99}
   {:text "Ciocolata se afla in bucatarie, sertarul numarul 3, cutia Yello Label!"}])

(defn answer-handler [*state answer]
  (fn [event]
    (.preventDefault event)
    (if (= answer (js/parseInt (get @*state :cur-answer)))
      (let [i (:index @*state)]
        (swap! *state conj {:index (mod (inc i) (count questions))
                            :cur-answer ""}))
      (swap! *state assoc :error? true))))


(defn game [*state]
  (fn []
    (println "Index:" (:index @*state))
    (let [i (:index @*state)
          {:keys [text ans time] :as q} (nth questions i)
          i (inc i)]
      [:div
       (when (< i (count questions)))
       [:h3 (str "Intrebarea " i " din " (count questions))]
       [:div.title
        text]
       (when ans
         [:div
          [:input.search-box
           {:type "text"
            :on-change #(swap! *state conj {:cur-answer (-> % .-target .-value)
                                            :error? false})
            :value (:cur-answer @*state)}]
          [:a.toggle-button
           {:on-click (answer-handler *state ans)}
           "Continuare"]])
       (when (:error? @*state)
         [:div "ERROR!"])])))




(defonce *state* (r/atom {:index 0
                          :cur-answer ""
                          :error? false}))

(defn puzzle-page [start-index]
  (fn []
    [:div.vert-container
     [:div.tv.noisy
      [:div.frame.tv
       [:div.piece.output [game *state*]]]]]))

