(ns plawww.medialist.toolbar)

(defn- toggle-button [props text]
  [:a.toggle-button props text])

(defn- button-all [*state]
  (fn []
    [toggle-button {:on-click #(swap! *state update-in [:show-all?] not)
                    :class-name (when (:show-all? @*state) :on)} "TOT"]))

(defn cycle-button
  "A button component which cycles through several possible texts/states, defined in `items`.
  When user clicks the button, the button transitions to the next state, displaying a new text.
  The state value is recorded in the `state` atom, in the keypath `ks`.
  `state` is an atom which contains current state (in the keypath defined by `ks`)
  `items` is a map with the keys being used as values in the atom state and values are the button titles.
  Example usage:

      [cycle-button
        (into (sorted-map) {:tag \"TAG\" :plain \"LIT\"})
        [:group-by]]
  "
  [items *state ks]
  (let [states (vec (keys items))
        titles (vec (vals items))
        ;Returns the next index based on current state 'cur'.
        next-index (fn [i]
                     (let [i (if (< i 0) 0 i)]
                          (mod (inc i) (count states))))]
    (fn [_ *state _]
      (let [cur (get-in @*state ks)
            i (.indexOf states cur)
            title (nth titles (next-index i))]
        [toggle-button
         {:on-click (fn []
                      (swap! *state update-in ks #(nth states (next-index (.indexOf states cur)))))}
         title]))))

(defn button-group-by [*state]
  [cycle-button
   (into (sorted-map) {:tag "TAG" :plain "LIT"})
   *state
   [:group-by]])

(defn button-av [*state]
  [cycle-button
   {:av "AV" :a "A." :v ".V"}
   *state
   [:av]])

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
         [button-group-by *state]]))))
