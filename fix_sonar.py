import re

def fix_file(filepath, replacements):
    with open(filepath, 'r') as f:
        content = f.read()

    for search, replace in replacements:
        if search in content:
            content = content.replace(search, replace)
            print(f"Fixed {search[:50]}... in {filepath}")
        else:
            print(f"Warning: Could not find {search[:50]}... in {filepath}")

    with open(filepath, 'w') as f:
        f.write(content)

# The Sonar failure on `0.0% coverage` with `26.9% duplication` on new code indicates
# the newly extracted `CommandParserUtil` test class does not have sufficient tests
# to achieve 80% coverage on the new utility file, OR the utility file itself
# is causing duplication/coverage issues.
