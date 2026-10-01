package ru.mirea.project.exception;

public class EntityNotFoundException extends BusinessException{
    public EntityNotFoundException (
        String entityName,
        long id
    ) {
        super(
            entityName+" с ID "+id+" не найден"
        );
    }
}