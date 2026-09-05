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

# MainUi is a presentation/design system. Persistence, backend and filesystem implementations
# belong to Core and must never leak into the shared visual layer.
for source in (ROOT / "main-ui" / "src").rglob("*.kt"):
    text = source.read_text(encoding="utf-8")
    rel = source.relative_to(ROOT)
    for forbidden in (
        "com.asdevelopers.academy.core.database.",
        "androidx.room.",
        "io.supabase.",
        "java.sql.",
        "java.io."
    ):
        require(forbidden not in text, f"{rel}: MainUi must not depend on implementation package {forbidden}")
    require("AcademyDatabase" not in text, f"{rel}: MainUi must not access Core database implementation")

runtime = (ROOT / "main-ui" / "src" / "main" / "kotlin" / "com" / "asdevelopers" / "academy" / "mainui" / "AcademyMainUiRuntime.kt").read_text(encoding="utf-8")
require("AcademyRuntime" in runtime, "MainUi runtime must adapt Core AcademyRuntime")
require("AcademyDatabase" not in runtime, "MainUi runtime must not create/access AcademyDatabase")

if errors:
    print("Foundation contract validation failed:")
    for error in errors:
        print(f" - {error}")
    sys.exit(1)

print("Foundation contract OK: MainUi is presentation-only and consumes Core public models/runtime")
