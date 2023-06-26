package compile.example

import future.keywords.in

allow {
  input.subject.clearance_level >= data.reports[_].clearance_level
}

allow {
  input.break_glass == true
}

test_set_in {
    num := input.entity.number
    num in {"one", "two", "three"}
}