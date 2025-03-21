package io.github.typesafegithub.workflows.dsl

import io.github.typesafegithub.workflows.domain.ActionStep
import io.github.typesafegithub.workflows.domain.CommandStep
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.domain.Job
import io.github.typesafegithub.workflows.domain.JobOutputs
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Shell
import io.github.typesafegithub.workflows.domain.actions.Action
import kotlinx.serialization.Contextual

@Suppress("LongParameterList")
@GithubActionsDsl
public class JobBuilder<OUTPUT : JobOutputs>(
    public val id: String,
    public val name: String?,
    public val runsOn: RunnerType,
    public val needs: List<Job<*>>,
    public val env: LinkedHashMap<String, String>,
    public val condition: String?,
    public val strategyMatrix: Map<String, List<String>>?,
    public val timeoutMinutes: Int? = null,
    public val concurrency: Concurrency? = null,
    public val jobOutputs: OUTPUT,
    override val _customArguments: Map<String, @Contextual Any?>,
) : HasCustomArguments {
    private var job = Job<OUTPUT>(
        id = id,
        name = name,
        runsOn = runsOn,
        needs = needs,
        condition = condition,
        env = env,
        steps = emptyList(),
        strategyMatrix = strategyMatrix,
        timeoutMinutes = timeoutMinutes,
        concurrency = concurrency,
        outputs = jobOutputs,
        _customArguments = _customArguments,
    )

    public fun run(
        command: String,
        env: LinkedHashMap<String, String> = linkedMapOf(),
        condition: String? = null,
        continueOnError: Boolean? = null,
        timeoutMinutes: Int? = null,
        shell: Shell? = null,
        workingDirectory: String? = null,
        @SuppressWarnings("FunctionParameterNaming")
        _customArguments: Map<String, @Contextual Any> = mapOf(),
    ): CommandStep = run(
        name = null,
        command = command,
        env = env,
        condition = condition,
        continueOnError = continueOnError,
        timeoutMinutes = timeoutMinutes,
        shell = shell,
        workingDirectory = workingDirectory,
        _customArguments = _customArguments,
    )

    public fun run(
        name: String? = null,
        command: String,
        env: LinkedHashMap<String, String> = linkedMapOf(),
        condition: String? = null,
        continueOnError: Boolean? = null,
        timeoutMinutes: Int? = null,
        shell: Shell? = null,
        workingDirectory: String? = null,
        @SuppressWarnings("FunctionParameterNaming")
        _customArguments: Map<String, @Contextual Any> = mapOf(),
    ): CommandStep {
        val newStep = CommandStep(
            id = "step-${job.steps.size}",
            name = name,
            command = command,
            env = env,
            condition = condition,
            continueOnError = continueOnError,
            timeoutMinutes = timeoutMinutes,
            shell = shell,
            workingDirectory = workingDirectory,
            _customArguments = _customArguments,
        )
        job = job.copy(steps = job.steps + newStep)
        return newStep
    }

    public fun <T : Action.Outputs> uses(
        action: Action<T>,
        env: LinkedHashMap<String, String> = linkedMapOf(),
        condition: String? = null,
        continueOnError: Boolean? = null,
        timeoutMinutes: Int? = null,
        @SuppressWarnings("FunctionParameterNaming")
        _customArguments: Map<String, @Contextual Any> = mapOf(),
    ): ActionStep<T> = uses(
        name = null,
        action = action,
        env = env,
        condition = condition,
        continueOnError = continueOnError,
        timeoutMinutes = timeoutMinutes,
        _customArguments = _customArguments,
    )

    public fun <T : Action.Outputs> uses(
        name: String? = null,
        action: Action<T>,
        env: LinkedHashMap<String, String> = linkedMapOf(),
        condition: String? = null,
        continueOnError: Boolean? = null,
        timeoutMinutes: Int? = null,
        @SuppressWarnings("FunctionParameterNaming")
        _customArguments: Map<String, @Contextual Any> = mapOf(),
    ): ActionStep<T> {
        val stepId = "step-${job.steps.size}"
        val newStep = ActionStep(
            id = stepId,
            name = name,
            action = action,
            env = env,
            condition = condition,
            continueOnError = continueOnError,
            timeoutMinutes = timeoutMinutes,
            outputs = action.buildOutputObject(stepId),
            _customArguments = _customArguments,
        )
        job = job.copy(steps = job.steps + newStep)
        return newStep
    }

    public fun build(): Job<OUTPUT> = job
}
