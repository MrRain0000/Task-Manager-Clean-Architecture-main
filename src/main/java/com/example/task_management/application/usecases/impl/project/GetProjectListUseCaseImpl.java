package com.example.task_management.application.usecases.impl.project;

import com.example.task_management.application.DTOUsecase.response.project.ProjectResult;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.ProjectRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.project.GetProjectListUseCase;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GetProjectListUseCaseImpl implements GetProjectListUseCase {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public GetProjectListUseCaseImpl(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ProjectResult> getMyProjects(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng hiện tại trong hệ thống."));

        List<ProjectMember> acceptedMemberships = projectMemberRepository
                .findAllByUserIdAndInvitationStatus(user.getId(), InvitationStatus.ACCEPTED);

        return acceptedMemberships.stream()
                .map(membership -> projectRepository.findById(membership.getProjectId()).orElse(null))
                .filter(Objects::nonNull)
                .map(project -> ProjectResult.builder()
                        .id(project.getId())
                        .name(project.getName())
                        .description(project.getDescription())
                        .ownerId(project.getOwnerId())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResult> getProjectsByOwner(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng hiện tại trong hệ thống."));

        return projectRepository.findAllByOwnerId(user.getId()).stream()
                .map(project -> ProjectResult.builder()
                        .id(project.getId())
                        .name(project.getName())
                        .description(project.getDescription())
                        .ownerId(project.getOwnerId())
                        .build())
                .collect(Collectors.toList());
    }
}
