import urllib.request
import json

project_key = "SSoggy-Group_SSoggySouls-for-Hardcore"
pr_number = "462"

url = f"https://sonarcloud.io/api/issues/search?componentKeys={project_key}&pullRequest={pr_number}"
try:
    req = urllib.request.Request(url)
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode())

        for issue in data.get('issues', []):
            print(f"Rule: {issue['rule']}")
            print(f"File: {issue['component']}")
            print(f"Line: {issue.get('line', 'N/A')}")
            print(f"Message: {issue['message']}\n")
except Exception as e:
    print(f"Error fetching issues: {e}")
