# Job outputs

It's possible to pass output from a job in a somewhat type-safe way (that is: types aren't checked, but the field names
are).

First, define `outputs` parameter in `job` function, inheriting from `JobOutputs`:

```kotlin hl_lines="4-7"
--8<-- "JobOutputsSnippets.kt:define-job-outputs-1"
--8<-- "JobOutputsSnippets.kt:define-job-outputs-2"
```

To set an output from within the job, use `jobOutputs`, and then an appropriate object field:

```kotlin
--8<-- "JobOutputsSnippets.kt:set-job-outputs"
```

and then use job's output from another job this way:

```kotlin hl_lines="9-10"
--8<-- "JobOutputsSnippets.kt:use-job-outputs"
```
