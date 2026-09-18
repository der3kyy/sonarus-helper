import json, struct, sys, zipfile
from pathlib import Path

root=Path(r"C:\Users\122\Documents\IDEA\Sonarus-Helper")
dist=root/"dist"
expected=["1.21.6","1.21.7","1.21.8","1.21.9","1.21.10","1.21.11","26.1","26.1.1","26.1.2","26.2","26.3"]
common=[
"win/sonarus/helper/SonarusHelperClient.class",
"win/sonarus/helper/config/SonarusHelperConfig.class",
"win/sonarus/helper/notifications/WindowsNotifier.class",
"win/sonarus/helper/notifications/ChatNotificationDetector.class",
"win/sonarus/helper/update/UpdateManager.class",
"win/sonarus/helper/gui/SonarusSettingsScreen.class",
"win/sonarus/helper/mixin/MinecraftClientMixin.class",
]
errors=[]; rows=[]

for version in expected:
    jar=dist/f"sonarus-helper-1.0-mc{version}.jar"
    if not jar.is_file():
        errors.append(f"MISSING {jar.name}"); continue
    java=">=21" if version.startswith("1.21.") else ">=25"
    major_expected=65 if version.startswith("1.21.") else 69
    try:
        with zipfile.ZipFile(jar) as z:
            names=set(z.namelist())
            meta=json.loads(z.read("fabric.mod.json").decode())
            mix=json.loads(z.read("sonarus_helper.mixins.json").decode())
            feature=z.read("win/sonarus/helper/features/dialog/EnterToJoinFeature.class")
            updater=z.read("win/sonarus/helper/update/UpdateManager.class")
            notifier=z.read("win/sonarus/helper/notifications/WindowsNotifier.class")
            classes=[z.read(n) for n in names if n.endswith(".class")]
    except Exception as e:
        errors.append(f"{jar.name}: inspect failed: {e}"); continue

    mv=str(meta.get("version")); mc=str(meta.get("depends",{}).get("minecraft"))
    jd=str(meta.get("depends",{}).get("java")); env=meta.get("environment")
    entry=meta.get("entrypoints",{}).get("client",[]); cmix=mix.get("client",[])

    if mv!="1.0": errors.append(f"{jar.name}: mod version {mv!r}")
    if mc!=version: errors.append(f"{jar.name}: minecraft {mc!r}")
    if jd!=java: errors.append(f"{jar.name}: java {jd!r}")
    if env!="client": errors.append(f"{jar.name}: environment {env!r}")
    if "sonarus_helper.mixins.json" not in meta.get("mixins",[]): errors.append(f"{jar.name}: mixin config missing")
    if "win.sonarus.helper.SonarusHelperClient" not in entry: errors.append(f"{jar.name}: client entrypoint missing")

    for name in common:
        if name not in names: errors.append(f"{jar.name}: missing {name}")

    chat="ChatHudMixin" if version.startswith("1.21.") else "ChatComponentMixin"
    chat_class=f"win/sonarus/helper/mixin/{chat}.class"
    if chat not in cmix: errors.append(f"{jar.name}: {chat} not registered")
    if "MinecraftClientMixin" not in cmix: errors.append(f"{jar.name}: MinecraftClientMixin not registered")
    if chat_class not in names: errors.append(f"{jar.name}: missing {chat_class}")

    major=struct.unpack(">H",feature[6:8])[0] if feature[:4]==b"\xca\xfe\xba\xbe" else -1
    if major!=major_expected: errors.append(f"{jar.name}: class major {major}, expected {major_expected}")
    if b"der3kyy/sonarus-helper" not in updater: errors.append(f"{jar.name}: updater repository constant missing")
    if b"java/awt/SystemTray" not in notifier: errors.append(f"{jar.name}: Windows notifier missing")
    if any(b"org/slf4j" in c or b"LoggerFactory" in c for c in classes): errors.append(f"{jar.name}: logging reference found")
    rows.append((version,jar.name,jar.stat().st_size,mv,mc,jd,major))

print("VERSION | JAR | BYTES | MOD | MINECRAFT | JAVA | CLASS")
for r in rows: print(" | ".join(map(str,r)))
print(f"\nVerified {len(rows)}/{len(expected)} expected JARs.")
if errors:
    print("ERRORS:")
    for e in errors: print("-",e)
    sys.exit(1)
print("ALL ARTIFACT AND FEATURE CHECKS PASSED")
