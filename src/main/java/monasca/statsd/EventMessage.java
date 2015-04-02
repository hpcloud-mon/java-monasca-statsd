package monasca.statsd;

import java.util.HashMap;
import java.util.Map;

/**
 * An event message Java bean that is used to hold an event object (which is sent to the datadog agent)
 * @author arnab
 */
public class EventMessage {

	private String title, message, aggregationKey, sourceTypeName;
	
	private long dateHappened;
	
	private Priority priority;
	
	private AlertType alterType;
	
	private Map<String, String> dimensions = new HashMap<String, String>();;

        /**
         */
        public EventMessage() {
        }
	
	/**
	 * @param title
	 * @param message
	 */
	public EventMessage(String title, String message) {
            this.title = title;
            this.message = message;
	}

	/**
	 * @return
	 */
	public String getTitle() {
		return title;
	}

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

	/**
	 * @return
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return
	 */
	public String getAggregationKey() {
		return aggregationKey;
	}

	/**
	 * 
	 * @param aggregationKey
	 */
	public void setAggregationKey(String aggregationKey) {
		this.aggregationKey = aggregationKey;
	}

	/**
	 * @return
	 */
	public String getSourceTypeName() {
		return sourceTypeName;
	}

	/**
	 * @param sourceTypeName
	 */
	public void setSourceTypeName(String sourceTypeName) {
		this.sourceTypeName = sourceTypeName;
	}

	/**
	 * @return
	 */
	public long getDateHappened() {
		return dateHappened;
	}

	/**
	 * @param dateHappened
	 */
	public void setDateHappened(long dateHappened) {
		this.dateHappened = dateHappened;
	}

	/**
	 * @return
	 */
	public Priority getPriority() {
		return priority;
	}

	/**
	 * @param priority
	 */
	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	/**
	 * @return
	 */
	public AlertType getAlterType() {
		return alterType;
	}

	/**
	 * @param alterType
	 */
	public void setAlterType(AlertType alterType) {
		this.alterType = alterType;
	}

	/**
	 * @return
	 */
	public Map<String, String> getDimensions() {
	    Map<String, String> returnMap = new HashMap <String, String>();
	    returnMap.putAll(dimensions);
		return returnMap;
	}

	/**
	 * @param dimensions
	 */
	public void setDimensions(Map<String, String> dimensions) {
		this.dimensions.putAll(dimensions);
	}
	
}
