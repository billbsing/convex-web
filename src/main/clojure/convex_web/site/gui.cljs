(ns convex-web.site.gui
  (:require [convex-web.site.format :as format]
            [convex-web.site.backend :as backend]
            [convex-web.site.stack :as stack]

            [clojure.string :as str]

            [reagent.core :as reagent]
            [reitit.frontend.easy :as rfe]
            [zprint.core :as zprint]

            ["highlight.js/lib/core" :as hljs]
            ["highlight.js/lib/languages/clojure"]
            ["highlight.js/lib/languages/javascript"]

            ["react-tippy" :as tippy]
            ["react-markdown" :as ReactMarkdown]

            ["@tailwindui/react" :as tailwindui-react]

            ["jdenticon" :as jdenticon]))

(defn event-target-value [event]
  (some-> event
          (.-target)
          (.-value)))

(defn scroll-into-view
  "https://developer.mozilla.org/en-US/docs/Web/API/Element/scrollIntoView"
  [id & [options]]
  (some-> (.getElementById js/document id)
          (.scrollIntoView (or (clj->js options) #js {}))))

(defn scroll-element-into-view
  "https://developer.mozilla.org/en-US/docs/Web/API/Element/scrollIntoView"
  [el]
  (when el
    (.scrollIntoView el)))

(defn highlight-block [el]
  (when el
    (.highlightBlock hljs el)))

(defn transaction-type-text-color [transaction-type]
  (case transaction-type
    :convex-web.transaction.type/transfer
    "text-indigo-500"

    :convex-web.transaction.type/invoke
    "text-pink-500"

    ""))

(defn transaction-type-description [transaction-type]
  (case transaction-type
    :convex-web.transaction.type/transfer
    "Direct transfer of Convex Coins from the Signer's Account to a destination Account"

    :convex-web.transaction.type/invoke
    "Execution of code by Signer Account"

    ""))

(defn account-type-text-color [account-status]
  (cond
    (get account-status :convex-web.account-status/library?)
    "text-purple-500"

    (get account-status :convex-web.account-status/actor?)
    "text-indigo-500"

    :else
    "text-green-500"))

(defn account-type-bg-color [account-status]
  (cond
    (get account-status :convex-web.account-status/library?)
    "bg-purple-500"

    (get account-status :convex-web.account-status/actor?)
    "bg-indigo-500"

    :else
    "bg-green-500"))

(defn account-type-label [account-status]
  (cond
    (get account-status :convex-web.account-status/library?)
    "library"

    (get account-status :convex-web.account-status/actor?)
    "actor"

    :else
    "user"))

(defn account-type-description [account-status]
  (cond
    (get account-status :convex-web.account-status/library?)
    "An immutable Account containing code and other static information. A
     Library is essentially an Actor with no exported functionality."

    (get account-status :convex-web.account-status/actor?)
    "An Autonomous Actor on the Convex network, which can be used to implement
     smart contracts."

    :else
    "An external user of Convex."))

(def identicon-size-small 26)
(def identicon-size-large 40)

(defn Identicon
  "Reagent wrapper for Jidenticon.

   - value is considered a hash string if the string is hexadecimal and
   contains at least 11 characters. It is otherwise considered a value that
   will be hashed using SHA1.

   - size defines the width and height, icons are always square, of the icon
   in pixels, including its padding.

   https://jdenticon.com"
  [{:keys [value size]}]
  [:div
   {:ref (fn [el]
           (when el
             (set! (.-innerHTML el) (jdenticon/toSvg value size))))}])

(defn Dismissible
  "Dismiss child component when clicking outside."
  [{:keys [on-dismiss]} child]
  (let [el (reagent/atom nil)

        handler (fn [e]
                  (when-let [el @el]
                    (when-not (.contains el (.-target e))
                      (on-dismiss))))]

    (reagent/create-class
      {:component-did-mount
       (fn [_]
         (.addEventListener js/document "click" handler false))

       :component-will-unmount
       (fn [_]
         (.removeEventListener js/document "click" handler false))

       :reagent-render
       (fn [_]
         [:div
          {:ref
           #(when %
              (reset! el %))}
          child])})))

(defn Transition
  "The Transition component lets you add enter/leave transitions to
   conditionally rendered elements, using CSS classes to control the actual
   transition styles in the different stages of the transition.

   enter: Applied the entire time an element is entering. Usually you define
   your duration and what properties you want to transition here, for example
   transition-opacity duration-75.

   enter-from: The starting point to enter from, for example opacity-0 if
   something should fade in.

   enter-to: The ending point to enter to, for example opacity-100 after fading in.

   leave: Applied the entire time an element is leaving. Usually you define
   your duration and what properties you want to transition here, for example
   transition-opacity duration-75.

   leave-from: The starting point to leave from, for example opacity-100 if something should fade out.

   leave-to: The ending point to leave to, for example opacity-0 after fading
   out.

   https://github.com/tailwindlabs/tailwindui-react#transition"
  [{:keys [show?
           enter
           enter-from
           enter-to
           leave
           leave-from
           leave-to]}
   child]
  [:> tailwindui-react/Transition
   {:show show?
    :enter enter
    :enterFrom enter-from
    :enterTo enter-to
    :leave leave
    :leaveFrom leave-from
    :leaveTo leave-to}

   child])

(def dropdown-transition
  {:enter "transition ease-out duration-100"
   :enter-from "transform opacity-0 scale-95"
   :enter-to "transform opacity-100 scale-100"
   :leave "transition ease-in duration-75"
   :leave-from "transform opacity-100 scale-100"
   :leave-to "transform opacity-0 scale-95"})

(defn ConvexLogo [& [attrs]]
  [:svg
   (merge {:viewBox "0 0 56 64"
           :version "1.1"}
          attrs)
   [:title "Convex logo"]
   [:desc "Created with Sketch."]
   [:g#v0.1 {:stroke "none" :stroke-width "1" :fill "none" :fill-rule "evenodd"}
    [:g#Home {:transform "translate(-692.000000, -139.000000)"}
     [:g#section-hero
      [:g#Hero-Block-Content {:transform "translate(256.000000, 139.000000)"}
       [:g#Convex-logo {:transform "translate(436.000000, 0.000000)"}
        [:polygon#Fill-1 {:fill "#132773" :points "28.0008 64 55.9988 48 55.9988 16.002"}]
        [:polygon#Fill-2 {:fill "#4B74CF" :points "0 47.9998 28 63.9998 0 15.9998"}]
        [:polygon#Fill-3 {:fill "#62A6E1" :points "28.0008 -0.0008 28.0008 63.9992 55.9988 16.0032 55.9988 15.9992"}]
        [:polygon#Fill-4 {:fill "#C3EAFC" :points "28.0008 -0.0008 0.0008 15.9992 28.0008 63.9992"}]]]]]]])

(defn ArrowCircleRightIcon [& [attrs]]
  [:svg.w-6.h-6
   (merge {:fill "none"
           :stroke "currentColor"
           :viewBox "0 0 24 24"
           :xmlns "http://www.w3.org/2000/svg"}
          attrs)
   [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M13 9l3 3m0 0l-3 3m3-3H8m13 0a9 9 0 11-18 0 9 9 0 0118 0z"}]])

(defn ArrowCircleDownIcon [& [attrs]]
  [:svg.w-6.h-6
   (merge {:fill "none"
           :stroke "currentColor"
           :viewBox "0 0 24 24"
           :xmlns "http://www.w3.org/2000/svg"}
          attrs)
   [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M15 13l-3 3m0 0l-3-3m3 3V8m0 13a9 9 0 110-18 9 9 0 010 18z"}]])

(defn InformationCircleIcon [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"}]])

(defn PlusIcon [& [attrs]]
  [:svg.w-6.h-6
   (merge {:fill "none"
           :stroke "currentColor"
           :viewBox "0 0 24 24"
           :xmlns "http://www.w3.org/2000/svg"}
          attrs)
   [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M12 6v6m0 0v6m0-6h6m-6 0H6"}]])

(defn PlayIcon [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"}] [:path {:d "M21 12a9 9 0 11-18 0 9 9 0 0118 0z"}]])

(defn BulletIcon [& [attrs]]
  [:svg
   (merge {:width "40px"
           :height "40px"
           :viewBox "0 0 40 40"
           :version "1.1"}
          attrs)
   [:title "Group"]
   [:g#Symbols {:stroke "none" :stroke-width "1" :fill "none" :fill-rule "evenodd"}
    [:g#Group
     [:rect#Rectangle {:fill "#F3F9FE" :x "0" :y "0" :width "40" :height "40" :rx "4"}]
     [:g#Icon {:transform "translate(4.000000, 4.000000)"}
      [:mask#mask-2 {:fill "white"}
       [:polygon#path-1 {:points "16.3031733 4 5.33333333 10.3333333 5.33333333 23 16.3031733 29.3333333 27.2725067 23 27.2725067 10.3333333"}]]
      [:polygon#path-1 {:fill "#62A6E1" :points "16.3031733 4 5.33333333 10.3333333 5.33333333 23 16.3031733 29.3333333 27.2725067 23 27.2725067 10.3333333"}]]]]])

(defn SortAscendingIcon [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M3 4h13M3 8h9m-9 4h6m4 0l4-4m0 0l4 4m-4-4v12"}]])

(defn SortDescendingIcon [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M3 4h13M3 8h9m-9 4h9m5-4v12m0 0l-4-4m4 4l4-4"}]])

(defn CopyClipboardIcon [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3"}]])

(defn CheckIcon [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M5 13l4 4L19 7"}]])

(defn IconXCircle [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
          attrs)
   [:path {:d "M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"}]])

(defn IconAdjustments [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :stroke "currentColor"
           :viewBox "0 0 24 24"}
          attrs)
   [:path {:d "M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4"}]])

(defn IconExternalLink [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :stroke "currentColor"
           :viewBox "0 0 24 24"}
          attrs)
   [:path {:d "M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"}]])

(defn IconUser [& [attrs]]
  [:svg
   (merge {:fill "none"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "2"
           :stroke "currentColor"
           :viewBox "0 0 24 24"}
          attrs)
   [:path {:d "M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"}]])

(defn IconChevronDown [& [attrs]]
  [:svg.chevron-down.w-6.h-6
   (merge {:viewBox "0 0 20 20"
           :fill "currentColor"}
          attrs)
   [:path {:fill-rule "evenodd" :d "M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" :clip-rule "evenodd"}]])

(defn GitHubIcon [& [attrs]]
  [:svg
   (merge {:xmlns "http://www.w3.org/2000/svg" :width "24" :height "24" :viewBox "0 0 24 24"}
          attrs)
   [:path {:d "M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"}]])

(defn Highlight [source & [{:keys [language pretty?]}]]
  (let [languages {:convex-lisp "language-clojure"
                   :convex-scrypt "language-javascript"}

        language (get languages language "language-clojure")

        source (str source)]
    [:div.shadow.overflow-auto
     [:pre.m-0
      [:code.text-xs.rounded
       {:class language
        :ref highlight-block}
       (cond
         (= language "language-clojure")
         (if pretty?
           (try (zprint/zprint-str source {:parse-string-all? true}) (catch js/Error _ source))
           source)

         :else
         source)]]]))

(defn SymbolType [type]
  [:div.px-1.border.rounded-full
   {:class (case type
             :function
             "bg-blue-100"

             :macro
             "bg-purple-100"

             :special
             "bg-orange-100"

             "bg-gray-100")}
   [:code.text-xs.text-gray-700 type]])

(defn SymbolStrip [{:keys [symbol syntax on-click]}]
  (let [{syntax-value :convex-web.syntax/value
         syntax-value-kind :convex-web.syntax/value-kind
         syntax-meta :convex-web.syntax/meta} syntax]
    [:div.flex.justify-between.items-center

     [:div
      {:class "flex flex-col w-1/2"}

      [:div.flex.items-center.space-x-2.py-1
       ;; -- Symbol
       [:code.font-bold.text-sm.text-indigo-500.hover:text-indigo-400.active:text-indigo-600
        {:class
         (if on-click
           "cursor-pointer"
           "cursor-default")
         :on-click (or on-click identity)}
        symbol]

       ;; -- Type
       (when syntax-value-kind
         [SymbolType syntax-value-kind])]

      ;; -- Value
      (when-not (str/blank? syntax-value)
        [:div.w-full [Highlight syntax-value]])]

     ;; -- Description
     [:div
      {:class "flex justify-between w-1/2"}
      (when-let [description (get-in syntax-meta [:doc :description])]
        [:p.text-sm.text-gray-800.ml-10 description])]]))

(defn SymbolMeta [{:keys [doc show-examples?] :or {show-examples? true}}]
  (let [{:keys [symbol examples type description signature]} doc]
    [:div.flex.flex-col.flex-1.text-sm.p-2
     [:div.flex.items-center

      ;; -- Symbol
      [:code.font-bold.mr-2 symbol]

      ;; -- Type
      (when type
        [SymbolType type])

      [:a.ml-2
       {:href (rfe/href :route-name/documentation-reference {} {:symbol symbol})
        :target "_blank"}
       [IconExternalLink {:class "h-4 w-4 text-gray-500 hover:text-black"}]]]

     ;; -- Signature
     [:div.flex.flex-col.my-2
      (for [{:keys [params]} signature]
        ^{:key params}
        [:code.text-xs (str params)])]

     ;; -- Description
     [:span.my-2 description]

     ;; -- Examples
     (when show-examples?
       (when (seq examples)
         [:div.flex.flex-col.items-start.my-2

          [:span.text-sm.text-black.text-opacity-75.mt-2.mb-1 "Examples"]

          (for [{:keys [code]} examples]
            ^{:key code}
            [:pre.text-xs.mb-1
             [:code.clojure.rounded
              {:ref highlight-block}
              code]])]))]))


(def hyperlink-hover-class "hover:underline hover:text-blue-500")

(def button-child-small-padding "px-6 py-2")
(def button-child-large-padding "px-8 py-3")

(defn ButtonText [{:keys [padding text-size text-color text-transform font-family]} text]
  [:span.block
   {:class [(or padding button-child-large-padding)
            (or text-size "text-sm")
            (or text-color "text-white")
            (or text-transform "uppercase")
            (or font-family "font-mono")]}
   text])

(defn Caption [text]
  [:span.text-gray-600.text-base.leading-none.cursor-default text])

(defn CaptionMono [text]
  [:span.font-mono.text-gray-600.text-base.leading-none.cursor-default text])

(def input-style "h-10 text-sm border rounded-md px-4 bg-blue-100 bg-opacity-25")

(defn DefaultButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge {:class
             ["text-sm"
              "px-2.5 py-1.5"
              "bg-gray-100"
              "rounded"
              "shadow-md"
              "focus:outline-none"
              "hover:opacity-75"
              "active:bg-gray-200"
              (if disabled?
                "text-gray-500 pointer-events-none")]
             :on-click identity}
            attrs)
     child]))

(defn BlackButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge {:class
             ["px-4 py-3"
              "bg-gray-900 hover:bg-gray-700 active:bg-black"
              "rounded"
              "shadow-md"
              "focus:outline-none"
              (if disabled?
                "pointer-events-none")]
             :on-click identity}
            attrs)
     child]))

(defn PrimaryButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge {:class
             ["bg-blue-500 hover:bg-blue-400 active:bg-blue-600"
              "rounded"
              "shadow-md"
              "focus:outline-none"
              (if disabled?
                "pointer-events-none")]
             :on-click identity}
            attrs)
     child]))

(defn SecondaryButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge {:class
             ["bg-indigo-500 hover:bg-indigo-400 active:bg-indigo-600"
              "rounded"
              "shadow-md"
              "focus:outline-none"
              (if disabled?
                "pointer-events-none")]
             :on-click identity}
            attrs)
     child]))

(defn LightBlueButton [attrs child]
  (let [disabled? (get attrs :disabled)]
    [:button
     (merge {:style
             {:background-color "#E5EEF9"}
             :class
             ["px-4 py-3"
              "rounded"
              "shadow-md"
              "focus:outline-none"
              "hover:bg-gray-200"
              "active:bg-gray-300"
              (if disabled?
                "pointer-events-none")]
             :on-click identity}
            attrs)
     child]))

(defn Tooltip
  "Reagent wrapper for React Tippy.

   https://github.com/tvkhoa/react-tippy#react-tippy"
  [attrs child]
  (let [attrs (if (string? attrs)
                {:title attrs}
                attrs)]
    [:> tippy/Tooltip attrs child]))


(defn InfoTooltip [tooltip]
  [Tooltip
   {:title tooltip}
   [InformationCircleIcon {:class "w-4 h-4 hover:text-gray-500"}]])


;; Select
;; ===============

(defn SelectOption [{:keys [id value] :as option}]
  (let [missing-id? (nil? id)
        value-not-str? (not (string? value))
        warn? (or missing-id? value-not-str?)
        group-label "SelectOption"

        value (if-not (string? value)
                (str value)
                value)

        id (or id value)]

    (when warn?
      (js/console.group group-label)

      (when missing-id?
        (js/console.warn "`:id` is missing;"))

      (when value-not-str?
        (js/console.warn "`:value` should be an string;"))

      (js/console.warn "In:" (prn-str option))

      (js/console.groupEnd group-label))

    [:option {:value id} value]))

(defn Select [{:keys [value options on-change]}]
  [:select
   {:class
    ["text-sm"
     "p-1"
     "rounded"
     "focus:outline-none"
     "bg-gray-100"]
    :value (or value "")
    :on-change (fn [event]
                 (on-change (.-value (.-target event))))}
   (for [option options]
     ^{:key option} [SelectOption {:value option}])])

(defn Select2
  "- `selected` is the `:id` value of option;

   - `options` is a collection of maps with keys `:id` and `:value`;

   - `on-change` will be called with the selected `:id`;"
  [{:keys [selected options on-change]}]
  [:select
   {:class
    ["text-sm"
     "p-1"
     "rounded"
     "focus:outline-none"
     "bg-gray-100 hover:shadow-md"]

    ;; `:value` is the React way to select a value.
    ;; Reference: https://reactjs.org/docs/forms.html#the-select-tag
    :value (or selected "")

    :on-change
    (fn [event]
      (let [id-value (.-value (.-target event))

            selected-id (some
                          (fn [{:keys [id]}]
                            (let [id-converted (if (or (keyword? id) (symbol? id))
                                                 (name id)
                                                 id)]
                              (when (= id-converted id-value)
                                id)))
                          options)]
        (on-change selected-id)))}
   (for [option options]
     ^{:key (:id option)} [SelectOption option])])

;; ----------------------------------------------------

(defn SpinnerSmall []
  [:div.spinner.ease-linear.rounded-full.border-2.border-t-2.border-gray-200.h-4.w-4])

(defn Spinner []
  [:div.spinner.ease-linear.rounded-full.border-4.border-t-4.border-gray-200.h-10.w-10])

(defn ClipboardCopy [text & [{:keys [color background-color hover margin]}]]
  (let [id (str (random-uuid))]
    [:div
     (merge {} (when margin {:class margin}))
     [:input.absolute
      {:id id
       :type "text"
       :value text
       :aria-hidden true
       :style {:left "-999em"}
       :on-change identity}]

     [Tooltip
      {:title "Copy"}
      [CopyClipboardIcon
       {:class
        ["w-4 h-4 cursor-pointer"
         (or color "text-gray-500")
         (or background-color "")
         (or (some->> hover (apply str)) "hover:text-black")]
        :on-click
        (fn []
          (.select (.getElementById js/document id))
          (.execCommand js/document "copy"))}]]]))

(defn Account [{:convex-web.account/keys [address status]}]
  (let [{:convex-web.account-status/keys [memory-size
                                          allowance
                                          balance
                                          sequence
                                          environment
                                          type]} status

        address-blob (format/prefix-0x address)

        caption-style "text-gray-600 text-base leading-none cursor-default"
        caption-container-style "flex flex-col space-y-1"
        value-style "text-sm cursor-default"

        Caption (fn [{:keys [label tooltip]}]
                  [:div.flex.space-x-1
                   [:span {:class caption-style} label]
                   [InfoTooltip tooltip]])]
    [:div.flex.flex-col.items-start.space-y-8

     ;; Address
     ;; ==============
     [:div.flex.flex-col
      [:div.flex.items-center
       ;; -- Identicon
       [Identicon {:value address :size 88}]

       ;; -- Address
       [:div {:class [caption-container-style "ml-4"]}
        [:span {:class caption-style} "Address"]
        [:span.inline-flex.items-center
         [:span.font-mono.text-base.mr-2 address-blob]
         [ClipboardCopy address-blob]]]]

      ;; -- Type
      [:span.inline-flex.justify-center.items-center.font-mono.text-xs.text-white.uppercase.mt-2.rounded
       {:style {:width "88px" :height "32px"}
        :class (account-type-bg-color status)}
       type]]


     ;; Balance
     ;; ==============
     [:div {:class caption-container-style}
      [Caption
       {:label "Balance"
        :tooltip "Account Balance denominated in Convex Copper Coins (the smallest coin unit)"}]
      [:code.text-2xl.cursor-default (format/format-number balance)]]


     ;; Memory
     ;; ==============
     [:div.flex.w-full {:class "space-x-1/6"}
      ;; -- Memory Allowance
      [:div {:class caption-container-style}
       [Caption
        {:label "Memory Allowance"
         :tooltip
         "Reserved Memory Allowance in bytes. If you create on-chain data
        beyond this amount, you will be charged extra transaction fees to
        aquire memory at the current memory pool price."}]
       [:code {:class value-style} allowance]]

      ;; -- Memory Size
      [:div {:class caption-container-style}
       [Caption
        {:label "Memory Size"
         :tooltip
         "Size in bytes of this Account, which includes any definitions you
          have created in your Enviornment."}]
       [:code {:class value-style} memory-size]]

      ;; -- Sequence
      [:div {:class caption-container-style}
       [Caption
        {:label "Sequence"
         :tooltip "Sequence number for this Account, which is equal to the
                    number of transactions that have been executed."}]
       [:code {:class value-style} (if (neg? sequence) "n/a" sequence)]]]


     ;; Environment
     ;; ==============
     [:div.w-full {:class caption-container-style}
      [Caption
       {:label "Environment"
        :tooltip
        "Environment, a space where reserved for each Account that can freely
         store on-chain data and definitions (e.g. code that you write in
         Convex Lisp)"}]
      [:div.flex.flex-col.items-center.w-full.px-10.overflow-auto.border.rounded.p-2
       [:div.flex.flex-col.w-full.divide-y
        (let [environment (sort-by (comp str first) environment)]
          (if (seq environment)
            (for [[symbol syntax] environment]
              ^{:key symbol}
              [:div.w-full.py-2.px-1
               [SymbolStrip
                {:symbol symbol
                 :syntax syntax
                 :on-click #(stack/push :page.id/environment-entry {:modal? true
                                                                    :state
                                                                    {:symbol symbol
                                                                     :syntax syntax}})}]])
            [:span.text-xs.text-gray-700.text-center "Empty"]))]]]]))

(defn RangeNavigation [{:keys [page-count
                               page-num
                               first-label
                               first-href
                               last-label
                               last-href
                               previous-href
                               previous-disabled?
                               next-href
                               next-disabled?
                               ajax/status]}]
  (let [action-style "block font-mono text-xs text-gray-800 hover:text-gray-500 hover:underline active:text-gray-900 uppercase"
        index-style "block font-mono text-xs text-gray-600 uppercase"]
    [:div.flex.space-x-8

     ;; Navigation
     ;; =============
     [:div.flex.items-center.space-x-4.px-2.py-1.border.border-gray-600.rounded
      ;; -- First
      [:a
       {:href first-href}
       [:span {:class action-style} (or first-label "Latest")]]

      ;; -- Previous
      [:a
       (merge {:href previous-href} (when previous-disabled?
                                      {:class "pointer-events-none"}))
       [:span {:class action-style} "Previous"]]

      ;; -- Next
      [:a
       (merge {:href next-href} (when next-disabled?
                                  {:class "pointer-events-none"}))
       [:span {:class action-style} "Next"]]

      ;; -- Last
      [:a
       {:href last-href}
       [:span {:class action-style} (or last-label "Earliest")]]]

     ;; Index
     ;; =============
     [:div.flex.items-center.space-x-4.px-2.py-1
      (case status
        :ajax.status/pending
        [:span {:class index-style}
         "Page _"]

        :ajax.status/success
        [:span {:class index-style}
         (str "Page " page-num " of " page-count)]

        "")]]))

(defn MarkdownCodeBlock [{:keys [value]}]
  [:pre.relative.bg-blue-100.bg-opacity-25
   [:div.absolute.right-0.top-0.m-2
    [ClipboardCopy value {:color "text-black"
                          :hover "hover:opacity-50"}]]

   [:code.hljs.language-clojure.text-sm.rounded
    {:ref highlight-block}
    value]])

(defn Markdown [markdown]
  [:> ReactMarkdown
   {:source markdown
    :renderers
    {:code (reagent/reactify-component MarkdownCodeBlock)}}])

(defn AccountSelect [{:keys [active-address addresses on-change]}]
  (let [state-ref (reagent/atom {:show? false})]
    (fn [{:keys [active-address addresses on-change]}]
      (let [{:keys [show?]} @state-ref

            item-style ["inline-flex w-full h-16 relative py-2 pl-3 pr-9"
                        "cursor-default select-none"
                        "text-gray-900 text-xs"
                        "hover:bg-blue-100 hover:bg-opacity-50 active:bg-blue-200"]]
        [:div

         ;; -- Selected
         [:button.h-10.inline-flex.items-center.justify-between.cursor-default.w-full.border.border-gray-200.rounded-md.bg-white.text-left.focus:outline-none.focus:shadow-outline-blue.focus:border-blue-300.transition.ease-in-out.duration-150.sm:text-sm.sm:leading-5
          {:on-click #(swap! state-ref update :show? not)}

          (if (str/blank? active-address)
            ;; Empty, but fill the space.
            [:div.flex-1]
            [:div.flex.flex-1.items-center.px-2
             [Identicon {:value (format/trim-0x active-address) :size 40}]

             [:span.font-mono.block.ml-2
              (format/prefix-0x active-address)]])

          [:svg.h-5.w-5.text-gray-400.pr-2.pointer-events-none
           {:viewBox "0 0 20 20" :fill "none" :stroke "currentColor"}
           [:path {:d "M7 7l3-3 3 3m0 6l-3 3-3-3" :stroke-width "1.5" :stroke-linecap "round" :stroke-linejoin "round"}]]]

         ;; -- Dropdown
         [:div.relative
          [Transition
           (merge dropdown-transition {:show? show?})
           [Dismissible
            {:on-dismiss #(swap! state-ref update :show? (constantly false))}
            [:div.origin-top-right.absolute.right-0.mt-2.rounded-md.shadow-lg.bg-white
             [:ul.max-h-60.rounded-md.py-1.text-base.leading-6.shadow-xs.overflow-auto.focus:outline-none.sm:text-sm.sm:leading-5

              (for [address addresses]
                ^{:key address}
                [:li
                 {:class item-style
                  :on-click
                  (fn []
                    (reset! state-ref {:show? false})

                    (when on-change
                      (on-change address)))}

                 [:div.flex.items-center
                  [:div.h-5.w-5.mr-2
                   (when (= address active-address)
                     [CheckIcon {:class "h-5 w-5"}])]

                  [Identicon {:value address :size 40}]

                  [:span.font-mono.block.ml-2
                   (format/prefix-0x address)]]])]]]]]]))))


(defn ObjectRendererAddress [object _]
  (reagent/with-let [account-ref (reagent/atom {:ajax/status :ajax.status/pending})

                     _ (backend/GET-account
                         object
                         {:handler
                          (fn [account]
                            (reset! account-ref {:account account
                                                 :ajax/status :ajax.status/success}))

                          :error-handler
                          (fn [error]
                            (js/console.error error)
                            (reset! account-ref {:ajax/status :ajax.status/error
                                                 :ajax/error error}))})]
    [:div.flex.flex-col.bg-white.rounded.shadow.p-2
     [:span.text-xs.text-indigo-500.uppercase "Address"]
     [:div.flex.items-center
      [:a.hover:underline.mr-2
       {:href (rfe/href :route-name/account-explorer {:address object})}
       [:div.flex.items-center

        ;; *Important*
        ;; Display identicon if and only if address is an existing Account.
        (when (= :ajax.status/success (:ajax/status @account-ref))
          [Identicon {:value object :size identicon-size-small}])

        [:code.text-xs (format/prefix-0x object)]]]

      [ClipboardCopy (format/prefix-0x object)]]

     (case (:ajax/status @account-ref)
       :ajax.status/pending
       [SpinnerSmall]

       :ajax.status/error
       [:span.text-xs.text-red-500 (get-in @account-ref [:ajax/error :response :error :message])]

       :ajax.status/success
       [:<>
        [:span.text-xs.text-indigo-500.uppercase.mt-2 "Balance"]
        (let [balance (get-in @account-ref [:account :convex-web.account/status :convex-web.account-status/balance])]
          [:span.text-xs.uppercase (format/format-number balance)])

        [:span.text-xs.text-indigo-500.uppercase.mt-2 "Type"]
        (let [type (get-in @account-ref [:account :convex-web.account/status :convex-web.account-status/type])]
          [:span.text-xs.uppercase type])])]))

(defn ObjectRenderer [object kind]
  (case kind
    :address
    [ObjectRendererAddress object kind]

    [Highlight (prn-str object)]))
