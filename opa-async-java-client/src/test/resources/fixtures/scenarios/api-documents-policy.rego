package test

allow {
    input.method == "GET"
    input.path[0] == "api"
    input.path[1] == "documents"
    input.user.group[_] == data.documents[_].group
}