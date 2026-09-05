The forge compilation error happens because we are calling `.withStyle(...)` on `MessageUtil.get(...)` which returns a `Component`. In modern versions (like 1.21), `Component` is an interface and `withStyle` might not be available directly on it, or maybe `MessageUtil.get()` returns something that doesn't have it.
Wait, if we look at `MessageUtil.java` inside forge:
`cat forge/src/main/java/org/ssoggy/ssoggysouls/util/MessageUtil.java`
