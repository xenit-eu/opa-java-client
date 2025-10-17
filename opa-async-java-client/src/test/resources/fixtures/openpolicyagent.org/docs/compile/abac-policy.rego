package abac.example

util.extract_content_type(header) := content_type {
	mime_type := trim_space(split(header, ";")[0])
	content_type := lower(mime_type)
}
default util.content_type_in(headers, accepted_content_types) := false

util.content_type_in(headers, accepted_content_types) {
	count(headers) == 1
	extracted_mime_type := util.extract_content_type(headers[0])
	extracted_mime_type == accepted_content_types[_]
}
default util.request.content_type_in(content_types) := false

util.request.content_type_in(content_types) {
	util.content_type_in(input.request.headers["content-type"], content_types)
}
default can_read_test := false

# - input.entity is type 'test'
can_read_test {
	input.auth.authenticated == true
	input.auth.principal.kind == "user"
	input.entity.a == input.auth.principal["contentgrid:att1"][_]
	input.entity.b == input.auth.principal["contentgrid:att2"][_]
}

# End policy bprnvz6ldw4q
# Policy nfgusztjwpba
# - input.entity is type 'test'
can_read_test {
	input.auth.authenticated == true
	input.auth.principal.kind == "user"
	input.auth.principal["contentgrid:admin"] == true
}

# End policy nfgusztjwpba
default allow := false

# Static definition Application Root
allow {
	input.request.method == ["HEAD", "GET"][_]
	# Path /
	count(input.request.path) == 0
}
allow {
	input.request.method == ["HEAD", "GET"][_]
	# Path /tests
	count(input.request.path) == 1
	input.request.path[0] == "tests"
	can_read_test == true
}
allow {
	input.request.method == ["HEAD", "GET"][_]
	# Path /tests/{id}
	count(input.request.path) == 2
	input.request.path[0] == "tests"
	# variable component {id}
	can_read_test == true
}
