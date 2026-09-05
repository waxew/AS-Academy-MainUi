#!/usr/bin/env python3
"""Validate real MainCourse folder packages with the same reference rules MainUi enforces."""

from __future__ import annotations

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


def validate_course(course_root: Path) -> list[str]:
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

    print(
        f"Validated {course_root}: "
        f"{len(levels)} levels, {len(chapters)} chapters, {len(lessons)} lessons, "
        f"{len(quizzes)} quizzes, {len(exercises)} exercises, {len(projects)} projects"
    )
    return errors


def main() -> int:
    if len(sys.argv) != 2:
        print("usage: validate_maincourse_snapshot.py <MainCourse/courses>", file=sys.stderr)
        return 2
    courses_root = Path(sys.argv[1])
    if not courses_root.is_dir():
        print(f"courses root not found: {courses_root}", file=sys.stderr)
        return 2

    candidates = sorted(
        path for path in courses_root.glob("*/course")
        if (path / "levels.json").is_file()
        and (path / "chapters.json").is_file()
        and (path / "lessons").is_dir()
    )
    if not candidates:
        print("no complete folder-based course packages found", file=sys.stderr)
        return 1

    all_errors: list[str] = []
    for course_root in candidates:
        try:
            errors = validate_course(course_root)
        except (OSError, json.JSONDecodeError, ValueError) as error:
            errors = [f"package read error: {error}"]
        all_errors.extend(f"{course_root}: {error}" for error in errors)

    if all_errors:
        print("\nMainCourse package validation failed:", file=sys.stderr)
        for error in all_errors:
            print(f"- {error}", file=sys.stderr)
        return 1

    print(f"All {len(candidates)} complete folder-based course package(s) passed validation.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
