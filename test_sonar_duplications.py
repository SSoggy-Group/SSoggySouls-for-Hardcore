import urllib.request
import json

project_key = "SSoggy-Group_SSoggySouls-for-Hardcore"
pr_number = "462"

url = f"https://sonarcloud.io/api/duplications/show?key={project_key}:forge/src/main/java/org/ssoggy/ssoggysouls/command/CommandRegistration.java&pullRequest={pr_number}"
try:
    req = urllib.request.Request(url)
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode())
        print("Forge Duplications:")
        print(json.dumps(data, indent=2))
except Exception as e:
    print(f"Error fetching Forge duplications: {e}")

url = f"https://sonarcloud.io/api/duplications/show?key={project_key}:neoforge/src/main/java/org/ssoggy/ssoggysouls/command/CommandRegistration.java&pullRequest={pr_number}"
try:
    req = urllib.request.Request(url)
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode())
        print("\nNeoForge Duplications:")
        print(json.dumps(data, indent=2))
except Exception as e:
    print(f"Error fetching NeoForge duplications: {e}")
