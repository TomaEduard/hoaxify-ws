package com.hoaxify.hoaxify.Hoax;

import com.hoaxify.hoaxify.Hoax.HoaxVM.HoaxVM;
import com.hoaxify.hoaxify.file.FileAttachment;
import com.hoaxify.hoaxify.file.FileAttachmentRepository;
import com.hoaxify.hoaxify.file.FileService;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

@Service
public class HoaxService {

    final HoaxRepository hoaxRepository;

    UserService userService;

    FileAttachmentRepository fileAttachmentRepository;

    FileService fileService;

    public HoaxService(HoaxRepository hoaxRepository, UserService userService, FileAttachmentRepository fileAttachmentRepository, FileService fileService) {
        this.hoaxRepository = hoaxRepository;
        this.userService = userService;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.fileService = fileService;
    }

    public Hoax save(User user, Hoax hoax) {
        hoax.setTimestamp(new Date());
        hoax.setUser(user);
        if (hoax.getAttachment() != null) {
            FileAttachment inDb = fileAttachmentRepository.findById(hoax.getAttachment().getId()).get();
            inDb.setHoax(hoax);
            hoax.setAttachment(inDb);
        }
        return hoaxRepository.save(hoax);
    }

    public Page<Hoax> getAllHoaxes(Pageable pageable) {
        return hoaxRepository.findAll(pageable);
    }

    public Page<Hoax> getHoaxesOfUser(String username, Pageable pageable) {
        User inDB = userService.getUserByUsername(username);
        return hoaxRepository.findByUser(inDB, pageable);
    }

    public Page<Hoax> getOldHoaxes(long id, String username, Pageable pageable) {
        Specification<Hoax> spec = Specification.where(idLessThan(id));
        if (username != null) {
            User inDB = userService.getUserByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return hoaxRepository.findAll(spec, pageable);
    }

//    public Page<Hoax> getOldHoaxes(long id, Pageable pageable) {
//        return hoaxRepository.findByIdLessThan(id, pageable);
//    }

//    public Page<Hoax> getOldHoaxesOfUser(long id, String username, Pageable pageable) {
//        User inDB = userService.getUserByUsername(username);
//        return hoaxRepository.findByIdLessThanAndUser(id, inDB, pageable);
//    }

    public List<Hoax> getNewHoaxes(long id, String username, Pageable pageable) {
        Specification<Hoax> spec = Specification.where(idGreaterThan(id));
        if (username != null) {
            User inDB = userService.getUserByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return hoaxRepository.findAll(spec, pageable.getSort()) ;
    }

//    public List<Hoax> getNewHoaxes(long id, Pageable pageable) {
//        return hoaxRepository.findByIdGreaterThan(id, pageable.getSort());
//    }

//    public List<Hoax> getNewHoaxesOfUser(long id, String username, Pageable pageable) {
//        User inDb = userService.getUserByUsername(username);
//        return hoaxRepository.findByIdGreaterThanAndUser(id, inDb, pageable.getSort()) ;
//    }

    public long getNewHoaxesCount(long id, String username) {
        Specification<Hoax> spec = Specification.where(idGreaterThan(id));
        if (username != null) {
            User inDB = userService.getUserByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return hoaxRepository.count(spec);
    }

//    public long getNewHoaxesCount(long id) {
//        return hoaxRepository.countByIdGreaterThan(id);
//    }

//    public long getNewHoaxesCountOfUser(long id, String username) {
//        User inDb = userService.getUserByUsername(username);
//        return hoaxRepository.countByIdGreaterThanAndUser(id, inDb);
//    }

    private Specification<Hoax> userIs(User user) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("user"), user);
        };
    }

    private Specification<Hoax> idLessThan(long id) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            return criteriaBuilder.lessThan(root.get("id"), id);
        };
    }

    private Specification<Hoax> idGreaterThan(long id) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            return criteriaBuilder.greaterThan(root.get("id"), id);
        };
    }

    public void deleteHoax(long id) {
        Hoax hoax = hoaxRepository.getOne(id);
        if (hoax.getAttachment() != null) {
            fileService.deleteAttachmentImage(hoax.getAttachment().getName());
        }
        hoaxRepository.deleteById(id);
    }
}
