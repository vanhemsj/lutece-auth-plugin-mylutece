package fr.paris.lutece.plugins.mylutece.util;

import fr.paris.lutece.plugins.mylutece.service.IUserParameterService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.util.CryptoService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.date.DateUtil;
import fr.paris.lutece.util.password.PasswordUtil;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;


/**
 * Util for security parameters
 * 
 */
public class SecurityUtils
{
    // MARKS
    private static final String MARK_FORCE_CHANGE_PASSWORD_REINIT = "force_change_password_reinit";
    private static final String MARK_PASSWORD_MINIMUM_LENGTH = "password_minimum_length";
    private static final String MARK_PASSWORD_FORMAT = "password_format";
    private static final String MARK_PASSWORD_DURATION = "password_duration";
    private static final String MARK_PASSWORD_HISTORY_SIZE = "password_history_size";
    private static final String MARK_MAXIMUM_NUMBER_PASSWORD_CHANGE = "maximum_number_password_change";
    private static final String MARK_TSW_SIZE_PASSWORD_CHANGE = "tsw_size_password_change";
    private static final String MARK_USE_ADVANCED_SECURITY_PARAMETERS = "use_advanced_security_parameters";
    private static final String MARK_ENABLE_PASSWORD_ENCRYPTION = "enable_password_encryption";
    private static final String MARK_ENCRYPTION_ALGORITHM = "encryption_algorithm";
    private static final String MARK_ACCOUNT_LIFE_TIME = "account_life_time";
    private static final String MARK_TIME_BEFORE_ALERT_ACCOUNT = "time_before_alert_account";
    private static final String MARK_NB_ALERT_ACCOUNT = "nb_alert_account";
    private static final String MARK_TIME_BETWEEN_ALERTS_ACCOUNT = "time_between_alerts_account";
    private static final String MARK_ACCES_FAILURES_MAX = "access_failures_max";
    private static final String MARK_ACCES_FAILURES_INTERVAL = "access_failures_interval";

    // MESSAGES
    private static final String MESSAGE_MINIMUM_PASSWORD_LENGTH = "mylutece.message.password.minimumPasswordLength";
    private static final String MESSAGE_PASSWORD_FORMAT = "mylutece.message.password.format";

    // ERROR
    private static final String ERROR_PASSWORD_MINIMUM_LENGTH = "password_minimum_length";
    private static final String ERROR_PASSWORD_WRONG_FORMAT = "password_format";
    private static final String ERROR_PASSWORD_ALREADY_USED = "password_already_used";
    private static final String ERROR_MAX_PASSWORD_CHANGE = "max_password_change";

    // PROPERTIES
    private static final String PROPERTY_DEFAULT_PASSWORD_MINIMAL_LENGTH = "security.defaultValues.passwordMinimalLength";
    private static final String PROPERTY_DEFAULT_MAXIMUM_NUMBER_PASSWORD_CHANGE = "security.defaultValues.maximumPasswordChange";
    private static final String PROPERTY_DEFAULT_TSW_SIZE_PASSWORD_CHANGE = "security.defaultValues.maximumPasswordChangeTSWSize";
    private static final String PROPERTY_DEFAULT_HISTORY_SIZE = "security.defaultValues.passwordHistorySize";
    private static final String PROPERTY_DEFAULT_PASSWORD_DURATION = "security.defaultValues.passwordDuration";
    private static final String PROPERTY_DEFAULT_ENCRYPTION_ALGORITHM = "security.defaultValues.algorithm";

    private static final String CONSTANT_DEFAULT_ENCRYPTION_ALGORITHM = "SHA-256";

    /**
     * Loads a model with base security parameters
     * @param parameterService The parameter service to use
     * @param model The base model to load
     * @param plugin The plugin
     * @return The model loaded with security parameters
     */
    public static Map<String, Object> checkSecurityParameters( IUserParameterService parameterService,
            Map<String, Object> model, Plugin plugin )
    {
        boolean bUseAdvancedParameters = getBooleanSecurityParameter( parameterService, plugin,
                MARK_USE_ADVANCED_SECURITY_PARAMETERS );
        model.put( MARK_ENABLE_PASSWORD_ENCRYPTION,
                getBooleanSecurityParameter( parameterService, plugin, MARK_ENABLE_PASSWORD_ENCRYPTION ) );
        model.put( MARK_ENCRYPTION_ALGORITHM, parameterService.getEncryptionAlgorithm( plugin ) );
        model.put( MARK_FORCE_CHANGE_PASSWORD_REINIT,
                isChangePasswordForceAfterReinitActivated( parameterService, plugin ) );
        model.put( MARK_PASSWORD_MINIMUM_LENGTH, getMinimumPasswordLength( parameterService, plugin ) );
        model.put( MARK_USE_ADVANCED_SECURITY_PARAMETERS, bUseAdvancedParameters );

        if ( bUseAdvancedParameters )
        {
            model.put( MARK_PASSWORD_FORMAT, isPasswordFormatUsed( parameterService, plugin ) );
            model.put( MARK_PASSWORD_DURATION, getPasswordDuration( parameterService, plugin ) );
            model.put( MARK_PASSWORD_HISTORY_SIZE, getPasswordHistorySize( parameterService, plugin ) );
            model.put( MARK_MAXIMUM_NUMBER_PASSWORD_CHANGE, getMaximumNumberPasswordChange( parameterService, plugin ) );
            model.put( MARK_TSW_SIZE_PASSWORD_CHANGE, getTSWSizePasswordChange( parameterService, plugin ) );
        }
        model.put( MARK_ACCOUNT_LIFE_TIME,
                getIntegerSecurityParameter( parameterService, plugin, MARK_ACCOUNT_LIFE_TIME ) );

        model.put( MARK_TIME_BEFORE_ALERT_ACCOUNT,
                getIntegerSecurityParameter( parameterService, plugin, MARK_TIME_BEFORE_ALERT_ACCOUNT ) );

        model.put( MARK_NB_ALERT_ACCOUNT, getIntegerSecurityParameter( parameterService, plugin, MARK_NB_ALERT_ACCOUNT ) );

        model.put( MARK_TIME_BETWEEN_ALERTS_ACCOUNT,
                getIntegerSecurityParameter( parameterService, plugin, MARK_TIME_BETWEEN_ALERTS_ACCOUNT ) );

        model.put( MARK_ACCES_FAILURES_MAX,
                getIntegerSecurityParameter( parameterService, plugin, MARK_ACCES_FAILURES_MAX ) );
        model.put( MARK_ACCES_FAILURES_INTERVAL,
                getIntegerSecurityParameter( parameterService, plugin, MARK_ACCES_FAILURES_INTERVAL ) );

        return model;
    }

    /**
     * Update security parameters from request parameters
     * @param parameterService Parameter service
     * @param request Request to get the parameter from
     * @param plugin The plugin
     */
    public static void updateSecurityParameters( IUserParameterService parameterService, HttpServletRequest request,
            Plugin plugin )
    {
        updateParameterValue( parameterService, plugin, MARK_FORCE_CHANGE_PASSWORD_REINIT,
                request.getParameter( MARK_FORCE_CHANGE_PASSWORD_REINIT ) );
        updateParameterValue( parameterService, plugin, MARK_PASSWORD_MINIMUM_LENGTH,
                request.getParameter( MARK_PASSWORD_MINIMUM_LENGTH ) );
        if ( getBooleanSecurityParameter( parameterService, plugin, MARK_USE_ADVANCED_SECURITY_PARAMETERS ) )
        {
            updateParameterValue( parameterService, plugin, MARK_PASSWORD_FORMAT,
                    request.getParameter( MARK_PASSWORD_FORMAT ) );
            updateParameterValue( parameterService, plugin, MARK_PASSWORD_DURATION,
                    request.getParameter( MARK_PASSWORD_DURATION ) );
            updateParameterValue( parameterService, plugin, MARK_PASSWORD_HISTORY_SIZE,
                    request.getParameter( MARK_PASSWORD_HISTORY_SIZE ) );
            updateParameterValue( parameterService, plugin, MARK_MAXIMUM_NUMBER_PASSWORD_CHANGE,
                    request.getParameter( MARK_MAXIMUM_NUMBER_PASSWORD_CHANGE ) );
            updateParameterValue( parameterService, plugin, MARK_TSW_SIZE_PASSWORD_CHANGE,
                    request.getParameter( MARK_TSW_SIZE_PASSWORD_CHANGE ) );
        }
        // Time of life of accounts 
        updateParameterValue( parameterService, plugin, MARK_ACCOUNT_LIFE_TIME,
                request.getParameter( MARK_ACCOUNT_LIFE_TIME ) );

        // Time before the first alert when an account will expire
        updateParameterValue( parameterService, plugin, MARK_TIME_BEFORE_ALERT_ACCOUNT,
                request.getParameter( MARK_TIME_BEFORE_ALERT_ACCOUNT ) );

        // Number of alerts sent to a user when his account will expire
        updateParameterValue( parameterService, plugin, MARK_NB_ALERT_ACCOUNT,
                request.getParameter( MARK_NB_ALERT_ACCOUNT ) );

        // Time between alerts
        updateParameterValue( parameterService, plugin, MARK_TIME_BETWEEN_ALERTS_ACCOUNT,
                request.getParameter( MARK_TIME_BETWEEN_ALERTS_ACCOUNT ) );

        updateParameterValue( parameterService, plugin, MARK_ACCES_FAILURES_MAX,
                request.getParameter( MARK_ACCES_FAILURES_MAX ) );

        updateParameterValue( parameterService, plugin, MARK_ACCES_FAILURES_INTERVAL,
                request.getParameter( MARK_ACCES_FAILURES_INTERVAL ) );
    }

    /**
     * Check whether a user must change his new password after a password
     * reset by mail.
     * @param parameterService Parameter service to get parameters from.
     * @param plugin The plugin
     * @return true if the user must, false otherwise
     */
    private static int getMinimumPasswordLength( IUserParameterService parameterService, Plugin plugin )
    {
        return getIntegerSecurityParameter( parameterService, plugin, MARK_PASSWORD_MINIMUM_LENGTH );
    }

    /**
     * Check whether a user must change his new password after a password reset
     * by mail.
     * @param parameterService Parameter service to get parameters from.
     * @param plugin The plugin
     * @return true if the user must, false otherwise
     */
    private static boolean isChangePasswordForceAfterReinitActivated( IUserParameterService parameterService,
            Plugin plugin )
    {
        Boolean bIsChecked = Boolean.valueOf( parameterService.findByKey( MARK_FORCE_CHANGE_PASSWORD_REINIT, plugin )
                .isChecked( ) );
        return bIsChecked.booleanValue( );
    }

    /**
     * Check whether a password is long enough.
     * @param strPassword Password to check
     * @param parameterService Parameter service to get parameters from.
     * @param plugin The plugin
     * @return true is the password is too short, or false if the password
     *         correct or if the password's minimum length is disabled.
     */
    protected static boolean checkUserPasswordMinimumLength( String strPassword,
            IUserParameterService parameterService, Plugin plugin )
    {
        int nMinimumLength = getIntegerSecurityParameter( parameterService, plugin, MARK_PASSWORD_MINIMUM_LENGTH );

        return !( nMinimumLength > 0 && strPassword.length( ) < nMinimumLength );
    }

    /**
     * Get the admin message telling the password length is too short.
     * @param request The request
     * @param parameterService Parameter service to get parameters from.
     * @param plugin The plugin
     * @return The URL of the admin message indicating that the entered password
     *         is too short.
     */
    protected static String getMessagePasswordMinimumLength( HttpServletRequest request,
            IUserParameterService parameterService, Plugin plugin )
    {
        Object[] param = { parameterService.findByKey( MARK_PASSWORD_MINIMUM_LENGTH, plugin ).getName( ) };

        return AdminMessageService.getMessageUrl( request, MESSAGE_MINIMUM_PASSWORD_LENGTH, param,
                AdminMessage.TYPE_STOP );
    }

    /**
     * Get the parameter indicating that a password must contain numbers.
     * @param parameterService Parameter service to get parameters from.
     * @param plugin The plugin
     * @return True if passwords must contain numbers, false otherwise
     */
    protected static boolean isPasswordFormatUsed( IUserParameterService parameterService, Plugin plugin )
    {
        return getBooleanSecurityParameter( parameterService, plugin, MARK_PASSWORD_FORMAT );
    }

    /**
     * Get the password duration
     * @param parameterService Parameter service to get parameters from.
     * @param plugin The plugin
     * @return The password duration, or 0 if no value or an incorrect value is
     *         specified
     */
    public static int getPasswordDuration( IUserParameterService parameterService, Plugin plugin )
    {
        return getIntegerSecurityParameter( parameterService, plugin, MARK_PASSWORD_DURATION );
    }

    /**
     * Get the password history size
     * @param parameterService Parameter service to get parameters from.
     * @param plugin The plugin
     * @return The password history size, or 0 if no value or an incorrect value
     *         is specified
     */
    public static int getPasswordHistorySize( IUserParameterService parameterService, Plugin plugin )
    {
        return getIntegerSecurityParameter( parameterService, plugin, MARK_PASSWORD_HISTORY_SIZE );
    }

    /**
     * Get the size of the time sliding window of passwor change
     * @param parameterService Parameter service to get parameters from.
     * @param plugin The plugin
     * @return the size of the time sliding window of passwor change, or 0 if
     *         none is specified
     */
    public static int getTSWSizePasswordChange( IUserParameterService parameterService, Plugin plugin )
    {
        return getIntegerSecurityParameter( parameterService, plugin, MARK_TSW_SIZE_PASSWORD_CHANGE );
    }

    /**
     * Get the parameter indicating that a password must contain numbers.
     * @param parameterService Parameter service to get parameters from.
     * @param plugin The plugin
     * @return True if passwords must contain numbers, false otherwise
     */
    public static boolean isAdvancedSecurityParametersUsed( IUserParameterService parameterService, Plugin plugin )
    {
        return getBooleanSecurityParameter( parameterService, plugin, MARK_USE_ADVANCED_SECURITY_PARAMETERS );
    }

    /**
     * Get the maximum number of time a user can change his password in a given
     * period
     * @param parameterService Parameter service to get parameters from.
     * @param plugin The plugin
     * @return The the maximum number of time a user can change his password in
     *         a given period, or 0 if no value or an incorrect value is
     *         specified
     */
    public static int getMaximumNumberPasswordChange( IUserParameterService parameterService, Plugin plugin )
    {
        return getIntegerSecurityParameter( parameterService, plugin, MARK_MAXIMUM_NUMBER_PASSWORD_CHANGE );
    }

    /**
     * Get the integer value of a security parameter
     * @param parameterService Parameter service to use
     * @param plugin The plugin
     * @param strParameterkey Key of the security parameter to get
     * @return The integer value of the security parameter
     */
    public static int getIntegerSecurityParameter( IUserParameterService parameterService, Plugin plugin,
            String strParameterkey )
    {
        ReferenceItem refItem = parameterService.findByKey( strParameterkey, plugin );
        if ( refItem == null || StringUtils.isEmpty( refItem.getName( ) ) )
        {
            return 0;
        }
        else
        {
            try
            {
                int nValue = Integer.parseInt( refItem.getName( ) );
                return nValue;
            }
            catch ( NumberFormatException e )
            {
                return 0;
            }
        }
    }

    /**
     * Get the boolean value of a security parameter
     * @param parameterService Parameter service to use
     * @param plugin The plugin
     * @param strParameterkey Key of the security parameter to get
     * @return The boolean value of the security parameter
     */
    public static boolean getBooleanSecurityParameter( IUserParameterService parameterService, Plugin plugin,
            String strParameterkey )
    {
        ReferenceItem refItem = parameterService.findByKey( strParameterkey, plugin );
        return refItem == null ? false : refItem.isChecked( );
    }

    /**
     * Check the format of the password from the entered parameters. The
     * password may have to contain upper and lower case letters, numbers and
     * special characters.
     * @param strPassword The password to check
     * @param parameterService Parameter service to get parameters from.
     * @param plugin The plugin
     * @return True if the giver parameter respect the parametered format, false
     *         if he violate one or more rules.
     */
    protected static boolean checkPasswordFormat( String strPassword, IUserParameterService parameterService,
            Plugin plugin )
    {
        boolean bPasswordFormat = isPasswordFormatUsed( parameterService, plugin );
        return bPasswordFormat ? PasswordUtil.checkPasswordFormat( strPassword ) : !bPasswordFormat;
    }

    /**
     * Gets the admin message saying that the password does not match the
     * required format
     * @param request The request
     * @param plugin The plugin
     * @return the url of the admin message saying that the password does not
     *         match the required format
     */
    protected static String getMessagePasswordFormat( HttpServletRequest request, Plugin plugin )
    {
        return AdminMessageService.getMessageUrl( request, MESSAGE_PASSWORD_FORMAT, AdminMessage.TYPE_STOP );
    }

    /**
     * Updates a parameter from its key with a new value.
     * @param parameterService Parameter service to use
     * @param plugin The plugin
     * @param strKey The key of the parameter to update
     * @param strValue The new value of the parameter
     */
    public static void updateParameterValue( IUserParameterService parameterService, Plugin plugin, String strKey,
            String strValue )
    {
        ReferenceItem userParam = new ReferenceItem( );
        userParam.setCode( strKey );
        strValue = strValue == null ? StringUtils.EMPTY : strValue;
        userParam.setName( strValue );
        parameterService.update( userParam, plugin );
    }

    /**
     * Enable advanced security parameters
     * @param parameterService Parameter service to use
     * @param plugin The plugin
     */
    public static void useAdvancedSecurityParameters( IUserParameterService parameterService, Plugin plugin )
    {
        updateParameterValue( parameterService, plugin, MARK_USE_ADVANCED_SECURITY_PARAMETERS, Boolean.TRUE.toString( ) );
        updateParameterValue( parameterService, plugin, MARK_FORCE_CHANGE_PASSWORD_REINIT, Boolean.TRUE.toString( ) );
        updateParameterValue( parameterService, plugin, MARK_MAXIMUM_NUMBER_PASSWORD_CHANGE,
                AppPropertiesService.getProperty( PROPERTY_DEFAULT_MAXIMUM_NUMBER_PASSWORD_CHANGE ) );
        updateParameterValue( parameterService, plugin, MARK_PASSWORD_DURATION,
                AppPropertiesService.getProperty( PROPERTY_DEFAULT_PASSWORD_DURATION ) );
        updateParameterValue( parameterService, plugin, MARK_PASSWORD_FORMAT, Boolean.TRUE.toString( ) );
        updateParameterValue( parameterService, plugin, MARK_PASSWORD_HISTORY_SIZE,
                AppPropertiesService.getProperty( PROPERTY_DEFAULT_HISTORY_SIZE ) );
        updateParameterValue( parameterService, plugin, MARK_TSW_SIZE_PASSWORD_CHANGE,
                AppPropertiesService.getProperty( PROPERTY_DEFAULT_TSW_SIZE_PASSWORD_CHANGE ) );

        int nMinPwdLength = getIntegerSecurityParameter( parameterService, plugin, MARK_PASSWORD_MINIMUM_LENGTH );
        if ( nMinPwdLength <= 0 )
        {
            updateParameterValue( parameterService, plugin, MARK_PASSWORD_MINIMUM_LENGTH,
                    AppPropertiesService.getProperty( PROPERTY_DEFAULT_PASSWORD_MINIMAL_LENGTH ) );
        }

        updateParameterValue( parameterService, plugin, MARK_ENABLE_PASSWORD_ENCRYPTION, Boolean.TRUE.toString( ) );
        updateParameterValue( parameterService, plugin, MARK_ENCRYPTION_ALGORITHM, AppPropertiesService.getProperty(
                PROPERTY_DEFAULT_ENCRYPTION_ALGORITHM, CONSTANT_DEFAULT_ENCRYPTION_ALGORITHM ) );

    }

    /**
     * Remove the advanced security parameters
     * @param parameterService Parameter service to use
     * @param plugin The plugin
     */
    public static void removeAdvancedSecurityParameters( IUserParameterService parameterService, Plugin plugin )
    {
        updateParameterValue( parameterService, plugin, MARK_USE_ADVANCED_SECURITY_PARAMETERS, StringUtils.EMPTY );
        updateParameterValue( parameterService, plugin, MARK_MAXIMUM_NUMBER_PASSWORD_CHANGE, StringUtils.EMPTY );
        updateParameterValue( parameterService, plugin, MARK_PASSWORD_DURATION, StringUtils.EMPTY );
        updateParameterValue( parameterService, plugin, MARK_PASSWORD_FORMAT, StringUtils.EMPTY );
        updateParameterValue( parameterService, plugin, MARK_PASSWORD_HISTORY_SIZE, StringUtils.EMPTY );
        updateParameterValue( parameterService, plugin, MARK_TSW_SIZE_PASSWORD_CHANGE, StringUtils.EMPTY );
    }

    /**
     * Get the current maximum valid date of a password from the parameter
     * service.
     * @param parameterService Parameter service to use
     * @param plugin The plugin
     * @return The maximum valid date of a password
     */
    public static Timestamp getPasswordMaxValidDate( IUserParameterService parameterService, Plugin plugin )
    {
        int nbDayPasswordValid = getIntegerSecurityParameter( parameterService, plugin, MARK_PASSWORD_DURATION );
        if ( nbDayPasswordValid <= 0 )
        {
            return null;
        }
        return PasswordUtil.getPasswordMaxValidDate( nbDayPasswordValid );
    }

    /**
     * Compute the maximum valid date of an account with the current time and
     * the parameters in the database.
     * @param parameterService Parameter service to use
     * @param plugin The plugin
     * @return The maximum valid date of an account
     */
    public static Timestamp getAccountMaxValidDate( IUserParameterService parameterService, Plugin plugin )
    {
        int nbMonthsPasswordValid = getIntegerSecurityParameter( parameterService, plugin, MARK_ACCOUNT_LIFE_TIME );
        if ( nbMonthsPasswordValid <= 0 )
        {
            return null;
        }
        Calendar calendare = new GregorianCalendar( Locale.getDefault( ) );
        calendare.add( Calendar.MONTH, nbMonthsPasswordValid );
        return new Timestamp( calendare.getTimeInMillis( ) );
    }

    /**
     * Test a password validity
     * @param parameterService Paramter service to use
     * @param plugin The plugin
     * @param strPassword The password to test validity
     * @param nUserId The id of the user
     * @return Returns null if the password is correct, or a code depending on
     *         the error found. Errors can be 'password_minimum_length' if the
     *         password is too short, or 'password_format' if the format of the
     *         password is not correct.
     */
    public static String checkPasswordForFrontOffice( IUserParameterService parameterService, Plugin plugin,
            String strPassword, int nUserId )
    {
        // Check minimum length password
        if ( !( SecurityUtils.checkUserPasswordMinimumLength( strPassword, parameterService, plugin ) ) )
        {
            return ERROR_PASSWORD_MINIMUM_LENGTH;
        }

        // Check password format
        if ( !( SecurityUtils.checkPasswordFormat( strPassword, parameterService, plugin ) ) )
        {
            return ERROR_PASSWORD_WRONG_FORMAT;
        }

        // Check password history
        if ( nUserId > 0 )
        {
            int nPasswordHistorySize = getIntegerSecurityParameter( parameterService, plugin,
                    MARK_PASSWORD_HISTORY_SIZE );
            if ( nPasswordHistorySize > 0 )
            {
                String strEncryptedPassword = buildPassword( parameterService, plugin, strPassword );
                List<String> passwordHistory = parameterService.selectUserPasswordHistory( nUserId, plugin );
                if ( nPasswordHistorySize < passwordHistory.size( ) )
                {
                    passwordHistory = passwordHistory.subList( 0, nPasswordHistorySize );
                }
                if ( passwordHistory.contains( strEncryptedPassword ) )
                {
                    return ERROR_PASSWORD_ALREADY_USED;
                }
            }

            int nTSWSizePasswordChange = getIntegerSecurityParameter( parameterService, plugin,
                    MARK_TSW_SIZE_PASSWORD_CHANGE );
            int nMaximumNumberPasswordChange = getIntegerSecurityParameter( parameterService, plugin,
                    MARK_MAXIMUM_NUMBER_PASSWORD_CHANGE );
            if ( nMaximumNumberPasswordChange > 0 )
            {
                Timestamp minDate = null;
                if ( nTSWSizePasswordChange > 0 )
                {
                    minDate = new Timestamp( new java.util.Date( ).getTime( )
                            - DateUtil.convertDaysInMiliseconds( nTSWSizePasswordChange ) );
                }
                else
                {
                    minDate = new Timestamp( 0 );
                }

                if ( parameterService.countUserPasswordHistoryFromDate( minDate, nUserId, plugin ) >= nMaximumNumberPasswordChange )
                {
                    return ERROR_MAX_PASSWORD_CHANGE;
                }
            }
        }

        return null;
    }

    /**
     * Test a password validity
     * @param parameterService Parameter service to use
     * @param plugin The plugin
     * @param strPassword Password to check
     * @param request The request
     * @return Returns null if the password is correct, or an admin message
     *         describing the error
     */
    public static String checkPasswordForBackOffice( IUserParameterService parameterService, Plugin plugin,
            String strPassword, HttpServletRequest request )
    {
        if ( !SecurityUtils.checkUserPasswordMinimumLength( strPassword, parameterService, plugin ) )
        {
            return SecurityUtils.getMessagePasswordMinimumLength( request, parameterService, plugin );
        }

        if ( !SecurityUtils.checkPasswordFormat( strPassword, parameterService, plugin ) )
        {
            return SecurityUtils.getMessagePasswordFormat( request, plugin );
        }
        return null;
    }

    /**
     * Build the password depending of the encryption.
     * If the encryption is enable, then it returns the password encrypted,
     * otherwise it just returns the password given in parameter.
     * @param parameterService The parameter service to use
     * @param plugin The plugin
     * @param strUserPassword the password
     * @return the password encrypted or not
     */
    public static String buildPassword( IUserParameterService parameterService, Plugin plugin, String strUserPassword )
    {
        // Check if there is an encryption algorithm
        String strPassword = strUserPassword;

        if ( parameterService.isPasswordEncrypted( plugin ) )
        {
            String strAlgorithm = parameterService.getEncryptionAlgorithm( plugin );
            strPassword = CryptoService.encrypt( strUserPassword, strAlgorithm );
        }

        return strPassword;
    }
}