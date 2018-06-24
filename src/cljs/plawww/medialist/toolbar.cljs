(ns plawww.medialist.toolbar)

(defn- toggle-button [props text]
  [:a.toggle-button props text])

(defn- button-all [*state]
  (fn []
    [toggle-button {:on-click #(swap! *state update-in [:show-all?] not)
                    :class-name (when (:show-all? @*state) :on)} "TOT"]))

(defn- button-tags [selected?]
  [toggle-button
   {:class-name (if selected? :on "")
    :href "/media/tag"} "TAG"])

(defn- button-letters [selected?]
  [toggle-button
   {:class-name (if selected? :on "")
    :href "/media/letter"} "LIT"])

(def av-states ["AV" "A." ".V"])

(defn button-av-handler [*state]
  (swap! *state update-in [:av] #(mod (inc %) (count av-states))))

(defn- button-av [*state]
  (let [i (int (:av @*state))
        title (nth av-states i)
        handler (fn [_] (swap! *state update-in [:av] #(mod (inc %) (count av-states))))]
    [toggle-button
     {:on-click handler
      :class-name (when (pos? i) :on)}
     title]))

(defn- button-item-details [*state]
  (fn []
    (let [detail-items? (:detail-items? @*state)]
      [toggle-button    {:on-click #(swap! *state update-in [:detail-items?] not)
                         :class-name (when detail-items? :on)}
       "DET"])))

(defn buttons [*state]
  (fn []
    (let [searching? (pos? (count (:search-string @*state)))]
      (when-not searching?
        [:div.toolbar.filters
         [button-all *state]
         [button-av *state]
         [button-item-details *state]
         [button-tags (= :tag (:group-by @*state))]
         [button-letters (= :plain (:group-by @*state))]]))))
