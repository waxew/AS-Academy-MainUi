#!/usr/bin/env python3
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
contract = json.loads((ROOT / "integration" / "contract.json").read_text(encoding="utf-8"))
errors = []

def require(condition, message):
    if not condition:
        errors.append(message)

require(contract.get("contractVersion") == 1, "contractVersion must be 1")
require(contract.get("contentSchemaVersion") == 1, "contentSchemaVersion must be 1")
require(contract.get("android", {}).get("minSdk") == 23, "minSdk must be 23")
require(contract.get("android", {}).get("compileSdk") == 36, "compileSdk must be 36")
require(contract.get("android", {}).get("javaVersion") == 17, "javaVersion must be 17")

build = (ROOT / "main-ui" / "build.gradle.kts").read_text(encoding="utf-8")
require(re.search(r"compileSdk\s*=\s*36", build) is not None, "MainUi compileSdk differs from contract")
require(re.search(r"minSdk\s*=\s*23", build) is not None, "MainUi minSdk differs from contract")
require("JavaVersion.VERSION_17" in build, "MainUi Java version differs from contract")
require(contract["coordinates"]["core"] in build, "MainUi must pin the Core coordinate from the foundation contract")
require("compileOnly" in build, "MainUi must consume Core as a host-owned compile dependency")

settings = (ROOT / "settings.gradle.kts").read_text(encoding="utf-8")
require("ACADEMY_CORE_DIR" in settings, "Standalone MainUi development must support ACADEMY_CORE_DIR")
require("includeBuild" in settings, "Standalone MainUi development must composite-build Core")

rules = contract.get("architectureRules", {})
require(rules.get("mainUiConsumesCorePublicApiOnly") is True, "MainUi public-API boundary is not enabled")

if errors:
    print("Foundation contract validation failed:")
    for error in errors:
        print(f" - {error}")
    sys.exit(1)

print("Foundation contract OK: MainUi is aligned with Core 1.4.0 and Android baseline")
