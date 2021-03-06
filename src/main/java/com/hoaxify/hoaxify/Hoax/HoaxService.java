package com.hoaxify.hoaxify.Hoax;

import com.hoaxify.hoaxify.file.FileAttachment;
import com.hoaxify.hoaxify.file.FileAttachmentRepository;
import com.hoaxify.hoaxify.file.FileService;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import com.hoaxify.hoaxify.user.UserService;
import com.hoaxify.hoaxify.userPreference.UserPreferenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class HoaxService {

    final HoaxRepository hoaxRepository;

    UserService userService;

    FileAttachmentRepository fileAttachmentRepository;

    FileService fileService;

    UserPreferenceRepository userPreferenceRepository;

    UserRepository userRepository;

    public HoaxService(HoaxRepository hoaxRepository, UserService userService, FileAttachmentRepository fileAttachmentRepository, FileService fileService, UserPreferenceRepository userPreferenceRepository, UserRepository userRepository) {
        this.hoaxRepository = hoaxRepository;
        this.userService = userService;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.fileService = fileService;
        this.userPreferenceRepository = userPreferenceRepository;
        this.userRepository = userRepository;
    }

    public Hoax save(long id, Hoax hoax) {
        User userDB = userRepository.findById(id).get();
//        User userDB = userRepository.findByUsername(username);
        hoax.setTimestamp(new Date());
        hoax.setUser(userDB);
        if (hoax.getAttachment() != null) {
            FileAttachment inDb = fileAttachmentRepository.findById(hoax.getAttachment().getId()).get();
            inDb.setHoax(hoax);
            hoax.setAttachment(inDb);
        }
        return hoaxRepository.save(hoax);
    }

    public Page<Hoax> getAllHoaxes(Pageable pageable) {
//        Page<Hoax> hoaxesInDb = hoaxRepository.findAlla(pageable, pageable.getSort());
        Page<Hoax> hoaxesInDb = hoaxRepository.findAll(pageable);
        return hoaxesInDb;
    }

//    public Page<Hoax> getAllHoaxes(Pageable pageable, User loggedInUser) {
//        Page<Hoax> hoaxesInDb = hoaxRepository.findAll(pageable);
//        if (loggedInUser == null) {
//            return hoaxesInDb;
//        }
//
//        User userByUsername = userService.getUserByUsername(loggedInUser.getUsername());
//        for (Hoax hoax : hoaxesInDb) {
//            UserPreference byHoaxIdAndUserId = userPreferenceRepository.findByHoaxIdAndUserId(hoax.getId(), userByUsername.getId());
//            hoax.setUserPreference((List<UserPreference>) byHoaxIdAndUserId);
//        }
//        return hoaxesInDb;
//    }

    public Page<Hoax> getHoaxesOfUser(String username, Pageable pageable) {
        User inDB = userService.getUserByUsername(username);
        return hoaxRepository.findByUser(inDB, pageable);
    }

    public Page<Hoax> getOldHoaxes(long id, Pageable pageable) {
        return hoaxRepository.findByIdLessThan(id, pageable);
    }

    public Page<Hoax> getOldHoaxesOfUser(long id, String username, Pageable pageable) {
        User inDB = userService.getUserByUsername(username);
        return hoaxRepository.findByIdLessThanAndUser(id, inDB, pageable);
    }

    public List<Hoax> getNewHoaxes(long id, Pageable pageable) {
        return hoaxRepository.findByIdGreaterThan(id, pageable.getSort());
    }

    public List<Hoax> getNewHoaxesOfUser(long id, String username, Pageable pageable) {
        User inDb = userService.getUserByUsername(username);

        return hoaxRepository.findByIdGreaterThanAndUser(id, inDb, pageable.getSort());
    }

    // sa aduc toate hoaxurile pe care userul le are ca favorite
//    public Page<Hoax> getNewHoaxesByPreferences(long id, long id, boolean favorite, Pageable pageable) {
//
//    }

    public long getNewHoaxesCount(long id) {
        return hoaxRepository.countByIdGreaterThan(id);
    }

    public long getNewHoaxesCountOfUser(long id, String username) {
        User inDb = userService.getUserByUsername(username);
        return hoaxRepository.countByIdGreaterThanAndUser(id, inDb);
    }

    public void deleteHoax(long id) {
        Hoax hoax = hoaxRepository.getOne(id);
        if (hoax.getAttachment() != null) {
            fileService.deleteAttachmentImage(hoax.getAttachment().getName());
        }
        hoaxRepository.deleteById(id);
    }

    //    public Page<Hoax> getOldHoaxes(long id, String username, Pageable pageable) {
//        Specification<Hoax> spec = Specification.where(idLessThan(id));
//        if (username != null) {
//            User inDB = userService.getUserByUsername(username);
//            spec = spec.and(userIs(inDB));
//        }
//        return hoaxRepository.findAll(spec, pageable);
//    }

    //    public List<Hoax> getNewHoaxes(long id, String username, Pageable pageable) {
//        Specification<Hoax> spec = Specification.where(idGreaterThan(id));
//        if (username != null) {
//            User inDB = userService.getUserByUsername(username);
//            spec = spec.and(userIs(inDB));
//        }
//        return hoaxRepository.findAll(spec, pageable.getSort()) ;
//    }

//    public long getNewHoaxesCount(long id, String username) {
//        Specification<Hoax> spec = Specification.where(idGreaterThan(id));
//        if (username != null) {
//            User inDB = userService.getUserByUsername(username);
//            spec = spec.and(userIs(inDB));
//        }
//        return hoaxRepository.count(spec);
//    }

//    private Specification<Hoax> userIs(User user) {
//        return (root, criteriaQuery, criteriaBuilder) -> {
//            return criteriaBuilder.equal(root.get("user"), user);
//        };
//    }
//
//    private Specification<Hoax> idLessThan(long id) {
//        return (root, criteriaQuery, criteriaBuilder) -> {
//            return criteriaBuilder.lessThan(root.get("id"), id);
//        };
//    }
//
//    private Specification<Hoax> idGreaterThan(long id) {
//        return (root, criteriaQuery, criteriaBuilder) -> {
//            return criteriaBuilder.greaterThan(root.get("id"), id);
//        };
//    }


}
