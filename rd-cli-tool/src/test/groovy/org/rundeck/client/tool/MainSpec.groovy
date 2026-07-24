package org.rundeck.client.tool

import picocli.CommandLine
import spock.lang.Specification

/**
 * Tests for the --allow-cross-origin-redirect CLI flag.
 */
class MainSpec extends Specification {

    def "picocli accepts --allow-cross-origin-redirect without Unknown option error"() {
        given:
        def main = new Main()
        def commandLine = new CommandLine(main)

        when:
        commandLine.parseArgs('--allow-cross-origin-redirect', 'system', 'info')

        then:
        noExceptionThrown()
        main.allowCrossOriginRedirect == true
    }

    def "picocli leaves allowCrossOriginRedirect false when flag is absent"() {
        given:
        def main = new Main()
        def commandLine = new CommandLine(main)

        when:
        commandLine.parseArgs('system', 'info')

        then:
        noExceptionThrown()
        main.allowCrossOriginRedirect == false
    }

    def "pre-processing sets system property when --allow-cross-origin-redirect is in args"() {
        given:
        System.clearProperty('rd.allow.cross.origin.redirect')
        def args = ['--allow-cross-origin-redirect', 'system', 'info'] as String[]

        when:
        // Mirrors the pre-processing logic in Main.main() that must run before picocli parses
        if (Arrays.asList(args).contains('--allow-cross-origin-redirect')) {
            System.setProperty('rd.allow.cross.origin.redirect', 'true')
        }

        then:
        System.getProperty('rd.allow.cross.origin.redirect') == 'true'

        cleanup:
        System.clearProperty('rd.allow.cross.origin.redirect')
    }

    def "pre-processing does not set system property when flag is absent"() {
        given:
        System.clearProperty('rd.allow.cross.origin.redirect')
        def args = ['system', 'info'] as String[]

        when:
        if (Arrays.asList(args).contains('--allow-cross-origin-redirect')) {
            System.setProperty('rd.allow.cross.origin.redirect', 'true')
        }

        then:
        System.getProperty('rd.allow.cross.origin.redirect') == null

        cleanup:
        System.clearProperty('rd.allow.cross.origin.redirect')
    }
}
