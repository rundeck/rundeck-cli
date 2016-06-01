package org.rundeck.util.toolbelt

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by greg on 6/1/16.
 */
@Unroll
class SimpleCommandInputSpec extends Specification {
    def "parse simple type #clazz expected #expected"() {
        given:
        def parser = new SimpleCommandInput()
        when:
        def arr = args.toArray(new String[args.size()])
        def value = parser.parseArgs(null, arr, clazz, paramName)

        then:
        value == expected

        where:
        args                   | clazz   | paramName  | expected
        ['--test', 'value']    | String  | 'test'     | 'value'
        ['-t', 'value']        | String  | 't'        | 'value'
        ['--mybool', 'xyz']    | Boolean | 'mybool'   | true
        ['--notmybool', 'xyz'] | Boolean | 'mybool'   | false
        ['-m', 'xyz']          | Boolean | 'm'        | true
        ['-z', 'xyz']          | Boolean | 'm'        | false
        ['--mybool', 'xyz']    | boolean | 'mybool'   | true
        ['--notmybool', 'xyz'] | boolean | 'mybool'   | false
        ['-m', 'xyz']          | boolean | 'm'        | true
        ['-z', 'xyz']          | boolean | 'm'        | false
        ['--someint', '123']   | Integer | 'someint'  | 123
        ['-s', '123']          | Integer | 's'        | 123
        ['--someint', '123']   | int     | 'someint'  | 123
        ['-s', '123']          | int     | 's'        | 123
        ['--somelong', '123']  | Long    | 'somelong' | 123L
        ['-s', '123']          | Long    | 's'        | 123L
        ['--somelong', '123']  | long    | 'somelong' | 123L
        ['-s', '123']          | long    | 's'        | 123L
        ['--afloat', '123']    | Float   | 'afloat'   | 123.0f
        ['-a', '123']          | Float   | 'a'        | 123.0f
        ['--afloat', '123.4']  | Float   | 'afloat'   | 123.4f
        ['-a', '123.4']        | Float   | 'a'        | 123.4f
        ['--afloat', '123']    | float   | 'afloat'   | 123.0f
        ['-a', '123']          | float   | 'a'        | 123.0f
        ['--afloat', '123.4']  | float   | 'afloat'   | 123.4f
        ['-a', '123.4']        | float   | 'a'        | 123.4f
        ['--adub', '123']      | Double  | 'adub'     | 123.0d
        ['-a', '123']          | Double  | 'a'        | 123.0d
        ['--adub', '123.4']    | Double  | 'adub'     | 123.4d
        ['-a', '123.4']        | Double  | 'a'        | 123.4d
        ['--adub', '123']      | double  | 'adub'     | 123.0d
        ['-a', '123']          | double  | 'a'        | 123.0d
        ['--adub', '123.4']    | double  | 'adub'     | 123.4d
        ['-a', '123.4']        | double  | 'a'        | 123.4d

    }

    def "parse simple invalid"() {
        given:
        def parser = new SimpleCommandInput()
        when:
        def arr = args.toArray(new String[args.size()])
        def value = parser.parseArgs(null, arr, clazz, paramName)

        then:
        IllegalArgumentException e = thrown()
        e.message == "Could not parse into a ${clazz.simpleName}: ${input}".toString()

        where:
        args                    | clazz   | paramName  | input
        ['--someint', '123.1']  | Integer | 'someint'  | '123.1'
        ['-s', '123.1']         | Integer | 's'        | '123.1'
        ['--somelong', '123.1'] | Long    | 'somelong' | '123.1'
        ['-s', '123.1']         | Long    | 's'        | '123.1'
        ['--afloat', '123 z']   | Float   | 'afloat'   | '123 z'
        ['-a', '123 z']         | Float   | 'a'        | '123 z'
        ['--afloat', '123 z']   | Float   | 'afloat'   | '123 z'
        ['-a', '123 z']         | Float   | 'a'        | '123 z'
        ['--adub', '123 z']     | Double  | 'adub'     | '123 z'
        ['-a', '123 z']         | Double  | 'a'        | '123 z'
        ['--adub', '123 z']     | Double  | 'adub'     | '123 z'
        ['-a', '123 z']         | Double  | 'a'        | '123 z'

    }
}
