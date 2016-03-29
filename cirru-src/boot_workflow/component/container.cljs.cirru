
ns boot-workflow.component.container $ :require
  [] hsl.core :refer $ [] hsl

def container-component $ {} (:name :container)
  :get-state $ fn (store)
    {}
  :update-state $ fn (old-state)
    , old-state
  :render $ fn (store)
    fn (state)
      [] :div ({})
        [] :span $ {} (:inner-text |Container)
