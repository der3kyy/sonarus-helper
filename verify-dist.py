import json
import struct
import sys
import zipfile
from pathlib import Path

root = Path(r"C:\Users\122\Documents\IDEA\Sonarus-Helper")
dist = root / "dist"
expected = [
    "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11",
    "26.1", "26.1.1", "26.1.2", "26.2", "26.3",
]

errors = []
rows = []

for version in expected:
    jar = dist / f"sonarus-helper-1.0-mc{version}.jar"
    if not jar.is_file():
        errors.append(f"MISSING {jar.name}")
        continue

    expected_java = ">=21" if version.startswith("1.21.") else ">=25"
    expected_major = 65 if version.startswith("1.21.") else 69

    try:
        with zipfile.ZipFile(jar) as archive:
            meta = json.loads(archive.read("fabric.mod.json").decode("utf-8"))
            feature = archive.read(
                "win/sonarus/helper/features/dialog/EnterToJoinFeature.class"
            )
            class_files = [
                archive.read(name)
                for name in archive.namelist()
                if name.endswith(".class")
            ]
    except Exception as exc:
        errors.append(f"{jar.name}: cannot inspect JAR: {exc}")
        continue

    mod_version = str(meta.get("version"))
    minecraft = str(meta.get("depends", {}).get("minecraft"))
    java_dep = str(meta.get("depends", {}).get("java"))
    environment = meta.get("environment")
    mixins = meta.get("mixins", [])

    if mod_version != "1.0":
        errors.append(f"{jar.name}: mod version is {mod_version!r}, expected '1.0'")
    if minecraft != version:
        errors.append(f"{jar.name}: minecraft dependency is {minecraft!r}, expected {version!r}")
    if java_dep != expected_java:
        errors.append(f"{jar.name}: java dependency is {java_dep!r}, expected {expected_java!r}")
    if environment != "client":
        errors.append(f"{jar.name}: environment is {environment!r}, expected 'client'")
    if "sonarus_helper.mixins.json" not in mixins:
        errors.append(f"{jar.name}: mixin config missing")

    if feature[:4] != b"\xCA\xFE\xBA\xBE":
        errors.append(f"{jar.name}: invalid class header")
        major = -1
    else:
        major = struct.unpack(">H", feature[6:8])[0]
        if major != expected_major:
            errors.append(
                f"{jar.name}: class major is {major}, expected {expected_major}"
            )

    if any(b"org/slf4j" in data or b"LoggerFactory" in data for data in class_files):
        errors.append(f"{jar.name}: logging reference found in compiled classes")

    rows.append(
        (version, jar.name, jar.stat().st_size, mod_version, minecraft, java_dep, major)
    )

print("VERSION | JAR | BYTES | MOD | MINECRAFT | JAVA | CLASS")
for row in rows:
    print(" | ".join(map(str, row)))

print()
print(f"Verified {len(rows)}/{len(expected)} expected JARs.")

if errors:
    print("ERRORS:")
    for error in errors:
        print("-", error)
    sys.exit(1)

print("ALL ARTIFACT CHECKS PASSED")
