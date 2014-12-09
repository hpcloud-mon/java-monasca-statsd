package com.ranartech.statsd;

public class EventMessage {

	private String title, message, aggregationKey, sourceTypeName;
	
	private long dateHappened;
	
	private Priority priority;
	
	private AlertType alterType;
	
	private String[] tags;
	
	public EventMessage(String title, String message) {
		super();
		this.title = title;
		this.message = message;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public String getAggregationKey() {
		return aggregationKey;
	}

	public void setAggregationKey(String aggregationKey) {
		this.aggregationKey = aggregationKey;
	}

	public String getSourceTypeName() {
		return sourceTypeName;
	}

	public void setSourceTypeName(String sourceTypeName) {
		this.sourceTypeName = sourceTypeName;
	}

	public long getDateHappened() {
		return dateHappened;
	}

	public void setDateHappened(long dateHappened) {
		this.dateHappened = dateHappened;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public AlertType getAlterType() {
		return alterType;
	}

	public void setAlterType(AlertType alterType) {
		this.alterType = alterType;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}
	
}
