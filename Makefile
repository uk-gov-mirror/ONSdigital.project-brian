.PHONY: all
all: audit test build

.PHONY: audit
audit:
	mvn ossindex:audit

.PHONY: build
build:
	mvn -Dmaven.test.skip -Dossindex.skip=true clean package dependency:copy-dependencies

.PHONY: debug
debug:
	./run.sh

.PHONY: test
test:
	mvn -Dossindex.skip=true test
