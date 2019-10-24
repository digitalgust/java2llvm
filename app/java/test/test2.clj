(ns test.test2)

(defn nn
  [x, y]
  (for
    [a (range x), b (range y)]
    [a b]
    )
  )

(print
  (nn 10 20)
  )


