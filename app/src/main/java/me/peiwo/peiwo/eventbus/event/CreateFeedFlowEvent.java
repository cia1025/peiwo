package me.peiwo.peiwo.eventbus.event;

import me.peiwo.peiwo.model.FeedFlowModel;

public class CreateFeedFlowEvent {
	public FeedFlowModel model = null;

	public CreateFeedFlowEvent(FeedFlowModel model) {
		this.model = model;
	}
}
