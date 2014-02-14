package com.rcslabs.rcl.icc;

import java.net.URL;

import com.rcslabs.rcl.core.IService;

public interface IAgentService extends IService {
	/**
	 * Возвращает URL на фото агента по agentId
	 * @return
	 */
	URL getPhotoUrl();
	
	/**
	 * Возвращает имя агента для отображения
	 * @return
	 */
	String getName();
	
	/**
	 * Возвращает список возможных ответов для оценки 
	 * работы агента
	 * @return
	 */
	IServiceEvaluation[] getPossibleServiceEvaluations();

	/**
	 * Возвращает в ICC выбранный пользователем вариант ответа при оценке качества работы.
	 * @param callId
	 * @param serviceEvaluationChoice
	 */
	void putServiceEvaluation(IServiceEvaluation serviceEvaluation); 	
}
