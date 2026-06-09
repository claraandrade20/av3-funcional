(ns trabfinal-api.db)

(def usuario (atom nil))

(def transacoes (atom '()))

(defn zerar! []
  (reset! usuario nil)
  (reset! transacoes '()))
