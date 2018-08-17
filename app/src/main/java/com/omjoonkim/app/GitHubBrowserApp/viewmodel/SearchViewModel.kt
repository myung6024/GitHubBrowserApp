package com.omjoonkim.app.GitHubBrowserApp.viewmodel

import com.omjoonkim.app.GitHubBrowserApp.rx.Parameter
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

abstract class SearchViewModel : BaseViewModel() {
    abstract val input: SearchViewModelInPuts
    abstract val output: SearchViewModelOutPuts
}

class SearchViewModelImpl : SearchViewModel() {

    private val name = PublishSubject.create<String>()
    private val clickSearchButton = PublishSubject.create<Parameter>()

    override val input = object : SearchViewModelInPuts {
        override fun name(name: String) =
            this@SearchViewModelImpl.name.onNext(name)

        override fun clickSearchButton(parameter: Parameter) =
            this@SearchViewModelImpl.clickSearchButton.onNext(parameter)
    }

    private val setEnabledSearchButton = PublishSubject.create<Boolean>()
    private val goResultActivity = PublishSubject.create<String>()
    override val output = object : SearchViewModelOutPuts {
        override fun setEnabledSearchButton(): Observable<Boolean> = setEnabledSearchButton

        override fun goResultActivity(): Observable<String> = goResultActivity
    }

    init {
        compositeDisposable.addAll(
            name.map { it.isNotEmpty() }
                .subscribe(setEnabledSearchButton::onNext),
            name.compose<String> { clickSearchButton.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(goResultActivity::onNext)
        )
    }
}

interface SearchViewModelInPuts : Input {
    fun name(name: String)
    fun clickSearchButton(parameter: Parameter)
}

interface SearchViewModelOutPuts : Output {
    fun setEnabledSearchButton(): Observable<Boolean>
    fun goResultActivity(): Observable<String>
}
