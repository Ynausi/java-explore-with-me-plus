package ru.practicum.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events",schema = "public")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "annotation",nullable = false,length = 255)
    private String annotation;

    @ManyToOne
    @JoinColumn(name = "categories_id",nullable = false)
    private Category category;

    @Column(name = "createdOn")
    private LocalDateTime createdOn;

    @Column(name = "description",length = 255)
    private String description;

    @Column(name = "eventDate",nullable = false)
    private LocalDateTime eventDate;


    @Column(name = "paid", nullable = false)
    private Boolean paid;

    @Column(name = "participantLimit")
    private Integer participantLimit;

    @Column(name = "publishedOn")
    private LocalDateTime publishedOn;

    @Column(name = "requestModeration")
    private Boolean requestModeration;
    
    @Column(name = "title",nullable = false,length = 255)
    private String title;

    @Column(name = "views")
    private Integer views;

}
