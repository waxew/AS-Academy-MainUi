#!/usr/bin/env python3
"""Validate release-ready MainCourse packages and audit legacy/migrating packages."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any, Iterable

REFERENCE_TYPES = {
    "EXERCISE": ("exerciseId", "exercise"),
    "EXERCISE_LINK": ("exerciseId", "exercise"),
    "QUIZ": ("quizId", "quiz"),
    "PROJECT": ("projectId", "project"),
    "PROJECT_LINK": ("projectId", "project"),
}


def objects_from_file(path: Path) -> list[dict[str, Any]]:
    payload = json.loads(path.read_text(encoding="utf-8"))
    if isinstance(payload, list):
        return [item for item in payload if isinstance(item, dict)]
    if isinstance(payload, dict):
        return [payload]
    raise ValueError(f"{path}: expected JSON object or array")


def objects_from_dir(path: Path) -> list[dict[str, Any]]:
    if not path.is_dir():
        return []
    result: list[dict[str, Any]] = []
    for file in sorted(path.rglob("*.json")):
        result.extend(objects_from_file(file))
    return result


def duplicate_ids(items: Iterable[dict[str, Any]]) -> set[str]:
    seen: set[str] = set()
    duplicates: set[str] = set()
    for item in items:
        value = str(item.get("id", "")).strip()
        if not value:
            continue
        if value in seen:
            duplicates.add(value)
        seen.add(value)
    return duplicates


def validate_course(course_root: Path) -> tuple[list[str], dict[str, int]]:
    errors: list[str] = []
    levels = objects_from_file(course_root / "levels.json")
    chapters = objects_from_file(course_root / "chapters.json")
    lessons = objects_from_dir(course_root / "lessons")
    quizzes = objects_from_dir(course_root / "quizzes")
    exercises = objects_from_dir(course_root / "exercises")
    projects = objects_from_dir(course_root / "projects")

    groups = {
        "level": levels,
        "chapter": chapters,
        "lesson": lessons,
        "quiz": quizzes,
        "exercise": exercises,
        "project": projects,
    }
    ids: dict[str, set[str]] = {}
    for kind, items in groups.items():
        values = {str(item.get("id", "")).strip() for item in items}
        blank_count = sum(1 for item in items if not str(item.get("id", "")).strip())
        if blank_count:
            errors.append(f"{kind}: {blank_count} item(s) have blank id")
        duplicates = duplicate_ids(items)
        if duplicates:
            errors.append(f"{kind}: duplicate id(s): {', '.join(sorted(duplicates))}")
        ids[kind] = {value for value in values if value}

    for chapter in chapters:
        chapter_id = str(chapter.get("id", "")).strip() or "<blank>"
        level_id = str(chapter.get("levelId", "")).strip()
        if not level_id or level_id not in ids["level"]:
            errors.append(f"chapter {chapter_id}: missing levelId target '{level_id}'")

    for lesson in lessons:
        lesson_id = str(lesson.get("id", "")).strip() or "<blank>"
        chapter_id = str(lesson.get("chapterId", "")).strip()
        if not chapter_id or chapter_id not in ids["chapter"]:
            errors.append(f"lesson {lesson_id}: missing chapterId target '{chapter_id}'")

        blocks = lesson.get("blocks") or []
        if not isinstance(blocks, list):
            errors.append(f"lesson {lesson_id}: blocks must be an array")
            continue
        for block in blocks:
            if not isinstance(block, dict):
                continue
            block_type = str(block.get("type", "")).strip().upper()
            reference = REFERENCE_TYPES.get(block_type)
            if reference is None:
                continue
            metadata = block.get("metadata") if isinstance(block.get("metadata"), dict) else {}
            key, target_kind = reference
            target_id = str(metadata.get(key, "")).strip()
            block_id = str(block.get("id", "")).strip() or "<blank>"
            if not target_id:
                errors.append(f"lesson {lesson_id} block {block_id}: blank {key}")
            elif target_id not in ids[target_kind]:
                errors.append(
                    f"lesson {lesson_id} block {block_id}: {key} '{target_id}' not found in {target_kind}s"
                )

    counts = {kind: len(items) for kind, items in groups.items()}
    return errors, counts


def discover_courses(courses_root: Path) -> dict[str, Path]:
    result: dict[str, Path] = {}
    for course_root in sorted(courses_root.glob("*/course")):
        if (
            (course_root / "levels.json").is_file()
            and (course_root / "chapters.json").is_file()
            and (course_root / "lessons").is_dir()
        ):
            result[course_root.parent.name] = course_root
    return result


def print_summary(course_id: str, counts: dict[str, int], prefix: str) -> None:
    print(
        f"{prefix} {course_id}: "
        f"{counts['level']} levels, {counts['chapter']} chapters, {counts['lesson']} lessons, "
        f"{counts['quiz']} quizzes, {counts['exercise']} exercises, {counts['project']} projects"
    )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("courses_root", type=Path)
    parser.add_argument(
        "--ready",
        action="append",
        default=[],
        metavar="COURSE_ID",
        help="Release-ready course that must pass validation. Repeatable.",
    )
    parser.add_argument(
        "--audit-all",
        action="store_true",
        help="Audit all other complete folder packages and report migration debt without failing CI.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    courses_root: Path = args.courses_root
    if not courses_root.is_dir():
        print(f"courses root not found: {courses_root}", file=sys.stderr)
        return 2

    courses = discover_courses(courses_root)
    if not courses:
        print("no complete folder-based course packages found", file=sys.stderr)
        return 1

    ready_ids = list(dict.fromkeys(args.ready))
    if not ready_ids:
        print("at least one --ready course must be supplied", file=sys.stderr)
        return 2

    hard_errors: list[str] = []
    for course_id in ready_ids:
        course_root = courses.get(course_id)
        if course_root is None:
            hard_errors.append(f"release-ready course '{course_id}' was not found as a complete folder package")
            continue
        try:
            errors, counts = validate_course(course_root)
        except (OSError, json.JSONDecodeError, ValueError) as error:
            errors, counts = [f"package read error: {error}"], {
                "level": 0, "chapter": 0, "lesson": 0, "quiz": 0, "exercise": 0, "project": 0
            }
        print_summary(course_id, counts, "READY")
        hard_errors.extend(f"{course_id}: {error}" for error in errors)

    audit_issue_count = 0
    if args.audit_all:
        print("\nMigration audit (non-blocking):")
        for course_id, course_root in courses.items():
            if course_id in ready_ids:
                continue
            try:
                errors, counts = validate_course(course_root)
            except (OSError, json.JSONDecodeError, ValueError) as error:
                errors, counts = [f"package read error: {error}"], {
                    "level": 0, "chapter": 0, "lesson": 0, "quiz": 0, "exercise": 0, "project": 0
                }
            print_summary(course_id, counts, "AUDIT")
            if errors:
                audit_issue_count += len(errors)
                preview = errors[:8]
                for error in preview:
                    print(f"::warning title=MainCourse migration debt ({course_id})::{error}")
                if len(errors) > len(preview):
                    print(
                        f"::warning title=MainCourse migration debt ({course_id})::"
                        f"{len(errors) - len(preview)} additional issue(s) omitted from annotation output; "
                        "see maincourse-validation.log"
                    )
                print(f"  {course_id}: {len(errors)} migration issue(s)")

    if hard_errors:
        print("\nRelease-ready MainCourse validation failed:", file=sys.stderr)
        for error in hard_errors:
            print(f"- {error}", file=sys.stderr)
        return 1

    print(
        f"\nRelease-ready validation passed for {len(ready_ids)} course(s): {', '.join(ready_ids)}. "
        f"Non-blocking migration audit found {audit_issue_count} issue(s)."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
