# logback-backlog

Classes that are missing in the logback library.

### BurstFilter

The BurstFilter is a logging filter that regulates logging traffic. Use this filter when you want to control
the maximum burst of log statements that can be sent to an appender. The filter is configured in the logback
configuration file. For example, the following configuration limits the number of INFO level (and lower) log
statements that can be sent to the console to a burst of 100 and allows a maximum of 10 log statements to be
sent to the appender every 6 seconds after that burst.

Here's a sample how to use it in a [logback.xml](src/test/resources/logback.xml):

```xml
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
  <layout class="ch.qos.logback.classic.PatternLayout">
    <Pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</Pattern>
  </layout>
  <Filter class="BurstFilter">
    <param name="level" value="INFO"/>
    <param name="burstRecoveryAmount" value="10"/>
    <param name="burstRecoveryInterval" value="6"/>
    <param name="maxBurst" value="100"/>
  </Filter>
</appender>
```