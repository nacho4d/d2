VERSION_FILE := build.gradle.kts
VERSION := $(shell grep '^version = ' $(VERSION_FILE) | sed 's/version = "//;s/"//')

MAJOR := $(word 1,$(subst ., ,$(VERSION)))
MINOR := $(word 2,$(subst ., ,$(VERSION)))
PATCH := $(word 3,$(subst ., ,$(VERSION)))

.DEFAULT_GOAL := help

.PHONY: help version bump-patch bump-minor bump-major build test run

help:
	@echo "Usage: make <target>"
	@echo ""
	@echo "  version      Show current version"
	@echo "  bump-patch   Bump patch version ($(MAJOR).$(MINOR).$(PATCH) -> $(MAJOR).$(MINOR).$(shell echo $$(($(PATCH)+1))))"
	@echo "  bump-minor   Bump minor version ($(MAJOR).$(MINOR).$(PATCH) -> $(MAJOR).$(shell echo $$(($(MINOR)+1))).0)"
	@echo "  bump-major   Bump major version ($(MAJOR).$(MINOR).$(PATCH) -> $(shell echo $$(($(MAJOR)+1))).0.0)"
	@echo "  build        Build plugin ZIP"
	@echo "  test         Run tests"
	@echo "  run          Launch sandboxed IDE"

version:
	@echo $(VERSION)

bump-patch:
	$(eval NEW_VERSION := $(MAJOR).$(MINOR).$(shell echo $$(($(PATCH)+1))))
	@sed -i '' 's/^version = "$(VERSION)"/version = "$(NEW_VERSION)"/' $(VERSION_FILE)
	@echo "$(VERSION) -> $(NEW_VERSION)"

bump-minor:
	$(eval NEW_VERSION := $(MAJOR).$(shell echo $$(($(MINOR)+1))).0)
	@sed -i '' 's/^version = "$(VERSION)"/version = "$(NEW_VERSION)"/' $(VERSION_FILE)
	@echo "$(VERSION) -> $(NEW_VERSION)"

bump-major:
	$(eval NEW_VERSION := $(shell echo $$(($(MAJOR)+1))).0.0)
	@sed -i '' 's/^version = "$(VERSION)"/version = "$(NEW_VERSION)"/' $(VERSION_FILE)
	@echo "$(VERSION) -> $(NEW_VERSION)"

build:
	./gradlew buildPlugin
	@echo "\nBuild output: build/distributions/d2-$(VERSION).zip"

test:
	./gradlew test

run:
	./gradlew runIde
