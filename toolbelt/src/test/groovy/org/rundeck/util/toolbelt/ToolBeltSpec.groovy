package org.rundeck.util.toolbelt

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by greg on 6/1/16.
 */
@Unroll
class ToolBeltSpec extends Specification {
    class MyTool1 {
        String name
        int age
        boolean leaving
        boolean greetResult

        @Command
        public boolean greet(@Arg("name") String name, @Arg("age") int age, @Arg("leaving") boolean leaving) {
            this.name = name
            this.age = age
            this.leaving = leaving
            greetResult
        }
    }

    class MyTool2 {
        String name
        int age
        boolean leaving
        boolean greetResult

        @Command(isSolo = true)
        public boolean greet(@Arg("name") String name, @Arg("age") int age, @Arg("leaving") boolean leaving) {
            this.name = name
            this.age = age
            this.leaving = leaving
            greetResult
        }
    }

    class TestOutput implements CommandOutput {
        List<Object> output = []
        List<Object> error = []
        List<Object> warning = []

        @Override
        void output(final Object output) {
            this.output << output
        }

        @Override
        void error(final Object error) {
            this.error << error
        }

        @Override
        void warning(final Object error) {
            this.warning << error
        }
    }

    def "bootstrap with param names"() {
        given:
        def test = new MyTool1()
        test.greetResult = true
        def tool = ToolBelt.with(test)
        when:
        def result = tool.runMain(['mytool1', 'greet', '--name', 'bob', '--age', '54', '--leaving'] as String[], false)
        then:
        result
        test.name == 'bob'
        test.age == 54
        test.leaving == true
    }

    def "bootstrap help with #helpCmd"() {
        given:
        def test = new MyTool1()
        test.greetResult = true
        def output = new TestOutput()
        def tool = ToolBelt.with(output, test)
        when:
        def result = tool.runMain(['mytool1', helpCmd] as String[], false)
        then:
        !result
        test.name == null
        test.age == 0
        test.leaving == false
        output.output == ['--name <String>', '--age <int>', '--leaving']

        where:
        helpCmd  | _
        '--help' | _
        'help' | _
        '-h' | _
        '?' | _
    }

    def "bootstrap tool fails"() {
        given:
        def test = new MyTool1()
        test.greetResult = false
        def tool = ToolBelt.with(test)
        when:
        def result = tool.runMain(['mytool1', 'greet', '--name', 'bob', '--age', '54', '--leaving'] as String[], false)
        then:
        !result
        test.name == 'bob'
        test.age == 54
        test.leaving == true
    }

    def " solo command"() {
        given:
        def test = new MyTool2()
        def tool = ToolBelt.with(test)
        when:
        def result = tool.runMain(['mytool2', '--name', 'bob', '--age', '54', '--leaving'] as String[], false)
        then:
        test.name == 'bob'
        test.age == 54
        test.leaving == true
        result == false
    }
}
