package felixstuff;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

public class ConsoleLogService implements LogService {
    private final Logger logger;

    public ConsoleLogService(LoggerFactory loggerFactory) {
        this.logger = loggerFactory.getLogger(ConsoleLogService.class);
    }

    @Override
    public void log(int level, String message) {
        switch (level) {
            case LOG_DEBUG:
                logger.debug(message);
                break;
            case LOG_INFO:
                logger.info(message);
                break;
            case LOG_WARNING:
                logger.warn(message);
                break;
            case LOG_ERROR:
                logger.error(message);
                break;
            default:
                logger.trace(message);
        }
    }

    @Override
    public void log(int level, String message, Throwable exception) {
        switch (level) {
            case LOG_DEBUG:
                logger.debug(message, exception);
                break;
            case LOG_INFO:
                logger.info(message, exception);
                break;
            case LOG_WARNING:
                logger.warn(message, exception);
                break;
            case LOG_ERROR:
                logger.error(message, exception);
                break;
            default:
                logger.trace(message, exception);
        }
    }

    @Override
    public void log(ServiceReference sr, int level, String message) {
        log(level, message);
    }

    @Override
    public void log(ServiceReference sr, int level, String message, Throwable exception) {
        log(level, message, exception);
    }

    @Override
    public Logger getLogger(String s) {
        return logger;
    }

    @Override
    public Logger getLogger(Class<?> aClass) {
        return logger;
    }

    @Override
    public <L extends Logger> L getLogger(String s, Class<L> aClass) {
        return null;
    }

    @Override
    public <L extends Logger> L getLogger(Class<?> aClass, Class<L> aClass1) {
        return null;
    }

    @Override
    public <L extends Logger> L getLogger(Bundle bundle, String s, Class<L> aClass) {
        return null;
    }
}